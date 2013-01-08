/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.wsf.stack.cxf.configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.cxf.Bus;
import org.apache.cxf.binding.soap.SoapTransportFactory;
import org.apache.cxf.buslifecycle.BusLifeCycleListener;
import org.apache.cxf.buslifecycle.BusLifeCycleManager;
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.resource.ResourceManager;
import org.apache.cxf.resource.ResourceResolver;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.apache.cxf.workqueue.AutomaticWorkQueue;
import org.apache.cxf.workqueue.AutomaticWorkQueueImpl;
import org.apache.cxf.workqueue.WorkQueueManager;
import org.apache.cxf.ws.policy.PolicyEngine;
import org.apache.cxf.ws.policy.selector.MaximalAlternativeSelector;
import org.jboss.ws.api.binding.BindingCustomization;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.UnifiedVirtualFile;
import org.jboss.wsf.spi.metadata.webservices.JBossWebservicesMetaData;
import org.jboss.wsf.stack.cxf.client.Constants;
import org.jboss.wsf.stack.cxf.deployment.WSDLFilePublisher;
import org.jboss.wsf.stack.cxf.interceptor.EnableDecoupledFaultInterceptor;
import org.jboss.wsf.stack.cxf.interceptor.EndpointAssociationInterceptor;
import org.jboss.wsf.stack.cxf.interceptor.NsCtxSelectorStoreInterceptor;

/**
 * A wrapper of the Bus for performing most of the configurations required on it by JBossWS
 * 
 * @author alessio.soldano@jboss.com
 * @since 25-Mar-2010
 *
 */
public abstract class BusHolder
{
   public static final String PARAM_CXF_BEANS_URL = "jbossws.cxf.beans.url";
   protected Bus bus;
   protected BusHolderLifeCycleListener busHolderListener;
   
   public BusHolder()
   {
      
   }
   
   public BusHolder(Bus bus)
   {
      setBus(bus);
   }
   
   /**
    * Update the Bus held by the this instance using the provided parameters.
    * This basically prepares the bus for being used with JBossWS.
    * 
    * @param soapTransportFactory   The SoapTransportFactory to configure, if any
    * @param resolver               The ResourceResolver to configure, if any
    * @param configurer             The JBossWSCXFConfigurer to install in the bus, if any
    * @param dep                    The current JBossWS-SPI Deployment
    */
   public void configure(SoapTransportFactory soapTransportFactory, ResourceResolver resolver, Configurer configurer, Deployment dep)
   {
      bus.setProperty(org.jboss.wsf.stack.cxf.client.Constants.DEPLOYMENT_BUS, true);
      busHolderListener = new BusHolderLifeCycleListener();
      bus.getExtension(BusLifeCycleManager.class).registerLifeCycleListener(busHolderListener);
      
      if (configurer != null)
      {
         bus.setExtension(configurer, Configurer.class);
      }
      setInterceptors(bus);
      setSoapTransportFactory(bus, soapTransportFactory);
      setResourceResolver(bus, resolver);
      
      //set MaximalAlternativeSelector on server side [JBWS-3149]
      if (bus.getExtension(PolicyEngine.class) != null) 
      {
         bus.getExtension(PolicyEngine.class).setAlternativeSelector(new MaximalAlternativeSelector());
      }
      
      if (dep != null)
      {
         setAdditionalWorkQueues(bus, dep.getAttachment(JBossWebservicesMetaData.class));
      }
   }
   
   
   /**
    * Performs close operations
    * 
    */
   public void close()
   {
      //call bus shutdown unless the listener tells us shutdown has already been asked
      if (busHolderListener == null || !busHolderListener.isPreShutdown())
      {
         bus.shutdown(true);
      }
      busHolderListener = null;
   }
   
   /**
    * A convenient method for getting a jbossws cxf server configurer
    * 
    * @param customization    The binding customization to set in the configurer, if any
    * @param wsdlPublisher    The wsdl file publisher to set in the configurer, if any
    * @param depEndpoints     The list of deployment endpoints
    * @return                 The new jbossws cxf configurer
    */
   public abstract Configurer createServerConfigurer(BindingCustomization customization,
         WSDLFilePublisher wsdlPublisher, List<Endpoint> depEndpoints, UnifiedVirtualFile root);
   
   protected static void setInterceptors(Bus bus)
   {
      //Install the EndpointAssociationInterceptor for linking every message exchange
      //with the proper spi Endpoint retrieved in CXFServletExt
      bus.getInInterceptors().add(new EndpointAssociationInterceptor());
      bus.getInInterceptors().add(new EnableDecoupledFaultInterceptor());
      bus.getInInterceptors().add(new NsCtxSelectorStoreInterceptor());
   }
   
   protected static void setResourceResolver(Bus bus, ResourceResolver resourceResolver)
   {
      if (resourceResolver != null)
      {
         bus.getExtension(ResourceManager.class).addResourceResolver(resourceResolver);
      }
   }
   
   protected static void setSoapTransportFactory(Bus bus, SoapTransportFactory factory)
   {
      if (factory != null)
      {
         DestinationFactoryManager dfm = bus.getExtension(DestinationFactoryManager.class);
         factory.setBus(bus);
         dfm.registerDestinationFactory(org.jboss.ws.common.Constants.NS_SOAP11, factory);
         dfm.registerDestinationFactory(org.jboss.ws.common.Constants.NS_SOAP12, factory);
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
   protected static void setAdditionalWorkQueues(Bus bus, JBossWebservicesMetaData wsmd)
   {
      if (wsmd != null) {
         Map<String, String> props = wsmd.getProperties();
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
