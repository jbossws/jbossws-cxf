/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.wsf.stack.cxf.configuration;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamReader;
import jakarta.xml.ws.handler.Handler;
import jakarta.xml.ws.soap.SOAPBinding;

import org.apache.cxf.Bus;
import org.apache.cxf.annotations.UseAsyncMethod;
import org.apache.cxf.buslifecycle.BusLifeCycleListener;
import org.apache.cxf.buslifecycle.BusLifeCycleManager;
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.endpoint.ServerLifeCycleManager;
import org.apache.cxf.interceptor.OneWayProcessorInterceptor;
import org.apache.cxf.management.InstrumentationManager;
import org.apache.cxf.management.counters.CounterRepository;
import org.apache.cxf.management.interceptor.ResponseTimeMessageInInterceptor;
import org.apache.cxf.management.interceptor.ResponseTimeMessageInvokerInterceptor;
import org.apache.cxf.management.interceptor.ResponseTimeMessageOutInterceptor;
import org.apache.cxf.resource.ResourceManager;
import org.apache.cxf.resource.ResourceResolver;
import org.apache.cxf.service.factory.FactoryBeanListener;
import org.apache.cxf.service.factory.FactoryBeanListenerManager;
import org.apache.cxf.service.invoker.Invoker;
import org.apache.cxf.staxutils.XMLStreamReaderWrapper;
import org.apache.cxf.transport.http.HttpDestinationFactory;
import org.apache.cxf.transport.servlet.ServletDestinationFactory;
import org.apache.cxf.workqueue.AutomaticWorkQueue;
import org.apache.cxf.workqueue.AutomaticWorkQueueImpl;
import org.apache.cxf.workqueue.WorkQueueManager;
import org.apache.cxf.ws.addressing.WSAddressingFeature;
import org.apache.cxf.ws.discovery.listeners.WSDiscoveryServerListener;
import org.apache.cxf.ws.policy.AlternativeSelector;
import org.apache.cxf.ws.policy.PolicyEngine;
import org.apache.cxf.ws.policy.selector.MaximalAlternativeSelector;
import org.apache.cxf.ws.rm.RMManager;
import org.apache.cxf.wsdl.WSDLManager;
import org.apache.cxf.wsdl11.WSDLManagerImpl;
import org.jboss.ws.api.annotation.PolicySets;
import org.jboss.ws.api.binding.BindingCustomization;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.WSFException;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.spi.deployment.AnnotationsInfo;
import org.jboss.wsf.spi.deployment.ArchiveDeployment;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.metadata.config.SOAPAddressRewriteMetadata;
import org.jboss.wsf.spi.metadata.webservices.JBossWebservicesMetaData;
import org.jboss.wsf.spi.security.JASPIAuthenticationProvider;
import org.jboss.wsf.stack.cxf.JBossWSInvoker;
import org.jboss.wsf.stack.cxf.addressRewrite.SoapAddressRewriteHelper;
import org.jboss.wsf.stack.cxf.client.Constants;
import org.jboss.wsf.stack.cxf.client.configuration.FeatureUtils;
import org.jboss.wsf.stack.cxf.client.configuration.InterceptorUtils;
import org.jboss.wsf.stack.cxf.client.configuration.JBossWSBusFactory;
import org.jboss.wsf.stack.cxf.client.configuration.JBossWSConfigurerImpl;
import org.jboss.wsf.stack.cxf.client.configuration.PropertyReferenceUtils;
import org.jboss.wsf.stack.cxf.deployment.EndpointImpl;
import org.jboss.wsf.stack.cxf.deployment.WSDLFilePublisher;
import org.jboss.wsf.stack.cxf.extensions.policy.PolicySetsAnnotationListener;
import org.jboss.wsf.stack.cxf.i18n.Loggers;
import org.jboss.wsf.stack.cxf.i18n.Messages;
import org.jboss.wsf.stack.cxf.interceptor.EndpointAssociationInterceptor;
import org.jboss.wsf.stack.cxf.interceptor.GracefulShutdownInterceptor;
import org.jboss.wsf.stack.cxf.interceptor.HandlerAuthInterceptor;
import org.jboss.wsf.stack.cxf.interceptor.NsCtxSelectorStoreInterceptor;
import org.jboss.wsf.stack.cxf.interceptor.WSDLSoapAddressRewriteInterceptor;
import org.jboss.wsf.stack.cxf.management.InstrumentationManagerExtImpl;
import org.jboss.wsf.stack.cxf.metadata.services.DDBeans;
import org.jboss.wsf.stack.cxf.metadata.services.DDEndpoint;
import org.jboss.wsf.stack.cxf.security.authentication.SubjectCreatingPolicyInterceptor;

/**
 * A wrapper of the Bus for performing most of the configurations required on it by JBossWS
 * 
 * @author alessio.soldano@jboss.com
 * @since 25-Mar-2010
 *
 */
public class BusHolder
{
   private boolean configured = false;

   protected DDBeans metadata;
   protected List<EndpointImpl> endpoints = new LinkedList<EndpointImpl>();
   
   protected Bus bus;
   protected BusHolderLifeCycleListener busHolderListener;
   protected FactoryBeanListener policySetsListener;
   
   public BusHolder()
   {
      
   }
   
   public BusHolder(Bus bus)
   {
      setBus(bus);
   }

   public BusHolder(DDBeans metadata)
   {
      super();
      this.metadata = metadata;
      bus = new JBossWSBusFactory().createBus();
      //Force servlet transport to prevent CXF from using Jetty / http server or other transports
      bus.setExtension(new ServletDestinationFactory(), HttpDestinationFactory.class);
   }

   /**
    * Update the Bus held by the this instance using the provided parameters.
    * This basically prepares the bus for being used with JBossWS.
    * 
    * @param resolver               The ResourceResolver to configure, if any
    * @param configurer             The JBossWSCXFConfigurer to install in the bus, if any
    * @param wsmd                   The current JBossWebservicesMetaData, if any
    * @param dep                    The current deployment
    */
   public void configure(ResourceResolver resolver, Configurer configurer, JBossWebservicesMetaData wsmd, Deployment dep)
   {
      if (configured)
      {
         throw Messages.MESSAGES.busAlreadyConfigured(bus);
      }
      
      bus.setProperty(org.jboss.wsf.stack.cxf.client.Constants.DEPLOYMENT_BUS, true);
      busHolderListener = new BusHolderLifeCycleListener();
      bus.getExtension(BusLifeCycleManager.class).registerLifeCycleListener(busHolderListener);
      setWSDLManagerStreamWrapper(bus);
      
      if (configurer != null)
      {
         bus.setExtension(configurer, Configurer.class);
      }
      Map<String, String> props = getProperties(wsmd);
      
      setInterceptors(bus, dep, props);
      dep.addAttachment(Bus.class, bus);

      try
      {
         final JASPIAuthenticationProvider jaspiProvider = SPIProvider.getInstance().getSPI(
               JASPIAuthenticationProvider.class,
               ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader());
         
         if (jaspiProvider != null && jaspiProvider.enableServerAuthentication(dep, wsmd))
         {
            bus.getInInterceptors().add(new SubjectCreatingPolicyInterceptor());
         }
      }
      catch (WSFException e)
      {
         Loggers.DEPLOYMENT_LOGGER.cannotFindJaspiClasses();
      }
      
      setResourceResolver(bus, resolver);
      
      if (bus.getExtension(PolicyEngine.class) != null) 
      {
         bus.getExtension(PolicyEngine.class).setAlternativeSelector(getAlternativeSelector(props));
      }     
      setCXFManagement(bus, props); //*first* enabled cxf management if required, *then* add anything else which could be manageable (e.g. work queues)
      setAdditionalWorkQueues(bus, props); 
      setWSDiscovery(bus, props);
      
      AnnotationsInfo ai = dep.getAttachment(AnnotationsInfo.class);
      if (ai == null || ai.hasAnnotatedClasses(PolicySets.class.getName())) {
         policySetsListener = new PolicySetsAnnotationListener(dep.getClassLoader());
         bus.getExtension(FactoryBeanListenerManager.class).addListener(policySetsListener);
      }
      
      //default to USE_ORIGINAL_THREAD = true; this can be overridden by simply setting the property in the endpoint or in the message using an interceptor
      //this forces one way operation to use original thread, which is required for ejb webserivce endpoints to avoid authorization failures from ejb container
      //and is a performance improvement in general when running in-container, as CXF needs to cache the message to free the thread, which is expensive
      //(moreover the user can tune the web container thread pool instead of expecting cxf to fork new threads)
      bus.setProperty(OneWayProcessorInterceptor.USE_ORIGINAL_THREAD, true);
      
      //[JBWS-3135] enable decoupled faultTo. This is an optional feature in cxf and we need this to be default to make it same behavior with native stack
      bus.setProperty("org.apache.cxf.ws.addressing.decoupled_fault_support", true);
      
      FeatureUtils.addFeatures(bus, bus, props);
      PropertyReferenceUtils.createPropertyReference(props, bus.getProperties());
      for (DDEndpoint dde : metadata.getEndpoints())
      {
         EndpointImpl endpoint = new EndpointImpl(bus, newInstance(dde.getImplementor(), dep));
         if (dde.getInvoker() != null)
            endpoint.setInvoker(newInvokerInstance(dde.getInvoker(), dep));
         endpoint.setAddress(dde.getAddress());
         endpoint.setEndpointName(dde.getPortName());
         endpoint.setServiceName(dde.getServiceName());
         endpoint.setWsdlLocation(dde.getWsdlLocation());
         setHandlers(endpoint, dde, dep);
         if (dde.getProperties() != null)
         {
            Map<String, Object> p = new HashMap<String, Object>();
            p.putAll(dde.getProperties());
            endpoint.setProperties(p);
         }
         if (dde.isAddressingEnabled()) 
         {
            WSAddressingFeature addressingFeature = new WSAddressingFeature();
            addressingFeature.setAddressingRequired(dde.isAddressingRequired());
            addressingFeature.setResponses(dde.getAddressingResponses());
            endpoint.getFeatures().add(addressingFeature);
         }
         endpoint.setPublishedEndpointUrl(dde.getPublishedEndpointUrl());
         endpoint.setSOAPAddressRewriteMetadata(dep.getAttachment(SOAPAddressRewriteMetadata.class));
         endpoint.publish();
         endpoints.add(endpoint);
         if (dde.isMtomEnabled())
         {
            SOAPBinding binding = (SOAPBinding) endpoint.getBinding();
            binding.setMTOMEnabled(true);
         }
      }
      configured = true;
   }
   
   @SuppressWarnings("rawtypes")
   private static void setHandlers(EndpointImpl endpoint, DDEndpoint dde, Deployment dep)
   {
      List<String> handlers = dde.getHandlers();
      if (handlers != null && !handlers.isEmpty())
      {
         List<Handler> handlerInstances = new LinkedList<Handler>();
         for (String handler : handlers)
         {
            handlerInstances.add((Handler) newInstance(handler, dep));
         }
         endpoint.setHandlers(handlerInstances);
      }
   }
   
   public void close()
   {
      //Move this stuff to the bus (our own impl)?
      RMManager rmManager = bus.getExtension(RMManager.class);
      if (rmManager != null)
      {
         rmManager.shutdown();
      }
      
      for (EndpointImpl endpoint : endpoints)
      {
         if (endpoint.isPublished())
         {
            endpoint.stop();
         }
      }
      endpoints.clear();
      
      //call bus shutdown unless the listener tells us shutdown has already been asked
      if (busHolderListener == null || !busHolderListener.isPreShutdown())
      {
         bus.shutdown(true);
      }
      busHolderListener = null;
      bus.getExtension(FactoryBeanListenerManager.class).removeListener(policySetsListener);
      policySetsListener = null;
   }

   private static Invoker newInvokerInstance(String className, Deployment dep)
   {
      final ClassLoader tccl = SecurityActions.getContextClassLoader();
      try
      {
         SecurityActions.setContextClassLoader(dep.getClassLoader());
         @SuppressWarnings("unchecked")
         Class<Invoker> clazz = (Class<Invoker>)tccl.loadClass(className);
         final AnnotationsInfo ai = dep.getAttachment(AnnotationsInfo.class);
         if (ai != null && clazz.isAssignableFrom(JBossWSInvoker.class)) {
            Constructor<Invoker> constr = clazz.getConstructor(boolean.class);
            return constr.newInstance(ai.hasAnnotatedClasses(UseAsyncMethod.class.getName()));
         } else {
            return clazz.getDeclaredConstructor().newInstance();
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         SecurityActions.setContextClassLoader(tccl);
      }
   }
   
   private static Object newInstance(String className, Deployment dep)
   {
      final ClassLoader tccl = SecurityActions.getContextClassLoader();
      try
      {
         SecurityActions.setContextClassLoader(dep.getClassLoader());
         Class<?> clazz = tccl.loadClass(className);
         return clazz.getDeclaredConstructor().newInstance();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         SecurityActions.setContextClassLoader(tccl);
      }
   }

   /**
    * A convenient method for getting a jbossws cxf server configurer
    * 
    * @param customization    The binding customization to set in the configurer, if any
    * @param wsdlPublisher    The wsdl file publisher to set in the configurer, if any
    * @param dep     The deployment
    * @return                 The new jbossws cxf configurer
    */
   public Configurer createServerConfigurer(BindingCustomization customization, WSDLFilePublisher wsdlPublisher, ArchiveDeployment dep)
   {
      ServerBeanCustomizer customizer = new ServerBeanCustomizer();
      customizer.setBindingCustomization(customization);
      customizer.setWsdlPublisher(wsdlPublisher);
      customizer.setDeployment(dep);
      return new JBossWSConfigurerImpl(customizer);
   }
   
   private static Map<String, String> getProperties(JBossWebservicesMetaData wsmd) {
      Map<String, String> props;
      if (wsmd != null) {
         props = wsmd.getProperties();
      } else {
         props = Collections.emptyMap();
      }
      return props;
   }
   
   protected void setInterceptors(Bus bus, Deployment dep, Map<String, String> props)
   {
      //Install the EndpointAssociationInterceptor for linking every message exchange
      //with the proper spi Endpoint retrieved in CXFServletExt
      bus.getInInterceptors().add(new EndpointAssociationInterceptor());
      bus.getInInterceptors().add(new NsCtxSelectorStoreInterceptor());
      bus.getInInterceptors().add(new GracefulShutdownInterceptor());
      
      final String p = (props != null) ? props.get(Constants.JBWS_CXF_DISABLE_HANDLER_AUTH_CHECKS) : null;
      if ((p == null || (!"true".equalsIgnoreCase(p) && !"1".equalsIgnoreCase(p))) && !Boolean.getBoolean(Constants.JBWS_CXF_DISABLE_HANDLER_AUTH_CHECKS)) {
         bus.getInInterceptors().add(new HandlerAuthInterceptor());
      } else {
         bus.getInInterceptors().add(new HandlerAuthInterceptor(true));
      }
      
      final SOAPAddressRewriteMetadata sarm = dep.getAttachment(SOAPAddressRewriteMetadata.class);
      if (SoapAddressRewriteHelper.isPathRewriteRequired(sarm) || SoapAddressRewriteHelper.isSchemeRewriteRequired(sarm)) {
         bus.getInInterceptors().add(new WSDLSoapAddressRewriteInterceptor(sarm));
      }
      
      InterceptorUtils.addInterceptors(bus, props);
   }
   
   protected static void setResourceResolver(Bus bus, ResourceResolver resourceResolver)
   {
      if (resourceResolver != null)
      {
         bus.getExtension(ResourceManager.class).addResourceResolver(resourceResolver);
      }
   }
   
   /**
    * Adds work queues parsing simple values of properties in jboss-webservices.xml:
    *   cxf.queue.<queue-name>.<parameter> = value
    * e.g.
    *   cxf.queue.default.maxQueueSize = 500
    * 
    * See constants in {@link org.jboss.wsf.stack.cxf.client.Constants}.
    * 
    * @param bus
    * @param wsmd
    */
   protected static void setAdditionalWorkQueues(Bus bus, Map<String, String> props)
   {
      if (props != null && !props.isEmpty()) {
         Map<String, Map<String, String>> queuesMap = new HashMap<String, Map<String,String>>();
         for (Entry<String, String> e : props.entrySet()) {
            String k = e.getKey();
            if (k.startsWith(Constants.CXF_QUEUE_PREFIX)) {
               String sk = k.substring(Constants.CXF_QUEUE_PREFIX.length());
               int i = sk.indexOf(".");
               if (i > 0) {
                  String queueName = sk.substring(0, i);
                  String queueProp = sk.substring(i+1);
                  Map<String, String> m = queuesMap.get(queueName);
                  if (m == null) {
                     m = new HashMap<String, String>();
                     queuesMap.put(queueName, m);
                  }
                  m.put(queueProp, e.getValue());
               }
            }
         }
         WorkQueueManager mgr = bus.getExtension(WorkQueueManager.class);
         for (Entry<String, Map<String, String>> e : queuesMap.entrySet()) {
            final String queueName = e.getKey();
            AutomaticWorkQueue q = createWorkQueue(queueName, e.getValue());
            mgr.addNamedWorkQueue(queueName, q);
         }
      }
   }
   
   protected static void setCXFManagement(Bus bus, Map<String, String> props) {
      if (props != null && !props.isEmpty()) {
         final String p = props.get(Constants.CXF_MANAGEMENT_ENABLED);
         if ("true".equalsIgnoreCase(p) || "1".equalsIgnoreCase(p)) {
            InstrumentationManagerExtImpl instrumentationManagerImpl = new InstrumentationManagerExtImpl();
            instrumentationManagerImpl.setBus(bus);
            instrumentationManagerImpl.setEnabled(true);
            instrumentationManagerImpl.init();
            instrumentationManagerImpl.initMBeanServer();            
            bus.setExtension(instrumentationManagerImpl, InstrumentationManager.class);
            CounterRepository couterRepository = new CounterRepository();
            couterRepository.setBus(bus);
            final String installRespTimeInterceptors = props.get(Constants.CXF_MANAGEMENT_INSTALL_RESPONSE_TIME_INTERCEPTORS);
            if (installRespTimeInterceptors == null ||
                  "true".equalsIgnoreCase(installRespTimeInterceptors) ||
                  "1".equalsIgnoreCase(installRespTimeInterceptors)) {
               ResponseTimeMessageInInterceptor in = new ResponseTimeMessageInInterceptor();
               ResponseTimeMessageInvokerInterceptor invoker = new ResponseTimeMessageInvokerInterceptor();
               ResponseTimeMessageOutInterceptor out = new ResponseTimeMessageOutInterceptor();
               bus.getInInterceptors().add(in);
               bus.getInInterceptors().add(invoker);
               bus.getOutInterceptors().add(out);
            }
            bus.setExtension(couterRepository, CounterRepository.class);
         }
      }
   }
   
   protected static void setWSDiscovery(Bus bus, Map<String, String> props) {
      if (props != null && !props.isEmpty()) {
         final String p = props.get(Constants.CXF_WS_DISCOVERY_ENABLED);
         if ("true".equalsIgnoreCase(p) || "1".equalsIgnoreCase(p)) {
            bus.getExtension(ServerLifeCycleManager.class).registerListener(new WSDiscoveryServerListener(bus));
         }
      }
   }
   
   private static void setWSDLManagerStreamWrapper(Bus bus)
   {
      ((WSDLManagerImpl) bus.getExtension(WSDLManager.class)).setXMLStreamReaderWrapper(new XMLStreamReaderWrapper()
      {
         @Override
         public XMLStreamReader wrap(XMLStreamReader reader)
         {
            return new SysPropExpandingStreamReader(reader);
         }
      });
   }
   
   private static AlternativeSelector getAlternativeSelector(Map<String, String> props) {
      //default to MaximalAlternativeSelector on server side [JBWS-3149]
      AlternativeSelector selector = new MaximalAlternativeSelector();
      if (props != null && !props.isEmpty()) {
         String className = props.get(Constants.CXF_POLICY_ALTERNATIVE_SELECTOR_PROP);
         if (className != null) {
            try {
               Class<?> clazz = Class.forName(className);
               selector = (AlternativeSelector)clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
               
            }
         }
      }
      return selector;
   }
   
   
   
   private static AutomaticWorkQueue createWorkQueue(String name, Map<String, String> props) {
      int mqs = parseInt(props.get(Constants.CXF_QUEUE_MAX_QUEUE_SIZE_PROP), 256);
      int initialThreads = parseInt(props.get(Constants.CXF_QUEUE_INITIAL_THREADS_PROP), 0);
      int highWaterMark = parseInt(props.get(Constants.CXF_QUEUE_HIGH_WATER_MARK_PROP), 25);
      int lowWaterMark = parseInt(props.get(Constants.CXF_QUEUE_LOW_WATER_MARK_PROP), 5);
      long dequeueTimeout = parseLong(props.get(Constants.CXF_QUEUE_DEQUEUE_TIMEOUT_PROP), 2 * 60 * 1000L);
      return new AutomaticWorkQueueImpl(mqs, initialThreads, highWaterMark, lowWaterMark, dequeueTimeout, name);
   }

   private static int parseInt(String prop, int defaultValue) {
      return prop != null ? Integer.parseInt(prop) : defaultValue;
   }
   
   private static long parseLong(String prop, long defaultValue) {
      return prop != null ? Long.parseLong(prop) : defaultValue;
   }
   
   /**
    * Return the hold bus
    * 
    * @return
    */
   public Bus getBus()
   {
      return bus;
   }
   
   protected void setBus(Bus bus)
   {
      this.bus = bus;
   }
   
   private static class BusHolderLifeCycleListener implements BusLifeCycleListener
   {
      private volatile boolean preShutdown = false;

      public boolean isPreShutdown()
      {
         return preShutdown;
      }

      @Override
      public void initComplete()
      {
         //NOOP
      }

      @Override
      public void preShutdown()
      {
         preShutdown = true;
      }

      @Override
      public void postShutdown()
      {
         //NOOP
      }
   }
}