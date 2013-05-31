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
package org.jboss.wsf.stack.cxf.client;

import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Endpoint;
import javax.xml.ws.EndpointContext;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.spi.Invoker;
import javax.xml.ws.spi.ServiceDelegate;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.DispatchImpl;
import org.apache.cxf.jaxws.ServiceImpl;
import org.apache.cxf.ws.addressing.EndpointReferenceType;
import org.jboss.ws.api.configuration.AbstractClientFeature;
import org.jboss.ws.common.configuration.ConfigHelper;
import org.jboss.ws.common.utils.DelegateClassLoader;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.spi.management.ServerConfigFactory;
import org.jboss.wsf.spi.metadata.config.ClientConfig;
import org.jboss.wsf.stack.cxf.Loggers;
import org.jboss.wsf.stack.cxf.Messages;
import org.jboss.wsf.stack.cxf.client.configuration.CXFClientConfigurer;
import org.jboss.wsf.stack.cxf.client.configuration.HandlerChainSortInterceptor;
import org.jboss.wsf.stack.cxf.client.configuration.JBossWSBusFactory;
import org.w3c.dom.Element;

/**
 * A custom javax.xml.ws.spi.Provider implementation
 * extending the CXF one while adding few customizations.
 * 
 * This also ensures a proper context classloader is set
 * (required on JBoss AS 7, as the TCCL does not include
 * implementation classes by default)
 * 
 * @author alessio.soldano@jboss.com
 * @since 27-Aug-2010
 *
 */
public class ProviderImpl extends org.apache.cxf.jaxws22.spi.ProviderImpl
{
   private static final boolean jbossModulesEnv;
   private static ServerConfig serverConfig = null;
   private static boolean serverConfigInit = false;
   
   static {
      //check if running in a JBoss Modules environment: the jbossws-cxf and cxf classes come
      //from different classloader when using jboss-modules (no flat classloader)
      jbossModulesEnv = (ProviderImpl.class.getClassLoader() != org.apache.cxf.jaxws22.spi.ProviderImpl.class.getClassLoader());
   }
   
   @Override
   protected org.apache.cxf.jaxws.EndpointImpl createEndpointImpl(Bus bus, String bindingId, Object implementor,
         WebServiceFeature... features)
   {
      Boolean db = (Boolean)bus.getProperty(Constants.DEPLOYMENT_BUS);
      if (db != null && db)
      {
         Loggers.ROOT_LOGGER.cannotUseCurrentDepBusForStartingNewEndpoint();
         bus = BusFactory.newInstance().createBus();
      }
      return super.createEndpointImpl(bus, bindingId, implementor, features);
   }
   
   @Override
   public Endpoint createEndpoint(String bindingId, Object implementor) {
      ClassLoader origClassLoader = getContextClassLoader();
      boolean restoreTCCL = false;
      try
      {
         restoreTCCL = checkAndFixContextClassLoader(origClassLoader);
         setValidThreadDefaultBus();
         return new DelegateEndpointImpl(super.createEndpoint(bindingId, implementor));
      }
      finally
      {
         if (restoreTCCL)
            setContextClassLoader(origClassLoader);
      }
   }
   
   @Override
   public Endpoint createEndpoint(String bindingId,
         Object implementor,
         WebServiceFeature ... features) {
      ClassLoader origClassLoader = getContextClassLoader();
      boolean restoreTCCL = false;
      try
      {
         restoreTCCL = checkAndFixContextClassLoader(origClassLoader);
         setValidThreadDefaultBus();
         return new DelegateEndpointImpl(super.createEndpoint(bindingId, implementor, features));
      }
      finally
      {
         if (restoreTCCL)
            setContextClassLoader(origClassLoader);
      }
   }
   
   @Override
   public Endpoint createEndpoint(String bindingId, Class<?> implementorClass,
         Invoker invoker, WebServiceFeature ... features) {
      ClassLoader origClassLoader = getContextClassLoader();
      boolean restoreTCCL = false;
      try
      {
         restoreTCCL = checkAndFixContextClassLoader(origClassLoader);
         setValidThreadDefaultBus();
         return new DelegateEndpointImpl(super.createEndpoint(bindingId, implementorClass, invoker, features));
      }
      finally
      {
         if (restoreTCCL)
            setContextClassLoader(origClassLoader);
      }
   }

   @SuppressWarnings("rawtypes")
   @Override
   public ServiceDelegate createServiceDelegate(URL url, QName qname, Class cls)
   {
      ClassLoader origClassLoader = getContextClassLoader();
      boolean restoreTCCL = false;
      try
      {
         restoreTCCL = checkAndFixContextClassLoader(origClassLoader);
         Bus bus = setValidThreadDefaultBus();
         return new JBossWSServiceImpl(bus, url, qname, cls);
      }
      finally
      {
         if (restoreTCCL)
            setContextClassLoader(origClassLoader);
      }
   }

   @SuppressWarnings("rawtypes")
   @Override
   public ServiceDelegate createServiceDelegate(URL wsdlDocumentLocation, QName serviceName, Class serviceClass,
         WebServiceFeature... features)
   {
      ClassLoader origClassLoader = getContextClassLoader();
      boolean restoreTCCL = false;
      try
      {
         restoreTCCL = checkAndFixContextClassLoader(origClassLoader);
         Bus bus = setValidThreadDefaultBus();
         for (WebServiceFeature f : features) {
             if (!f.getClass().getName().startsWith("javax.xml.ws")) {
                 throw Messages.MESSAGES.unknownFeature(f.getClass().getName());
             }
         }
         return new JBossWSServiceImpl(bus, wsdlDocumentLocation, serviceName, serviceClass, features);
      }
      finally
      {
         if (restoreTCCL)
            setContextClassLoader(origClassLoader);
      }
   }
   
   /**
    * Ensure the current context classloader can load this ProviderImpl class.
    * 
    * @return true if the TCCL has been changed, false otherwise
    */
   static boolean checkAndFixContextClassLoader(ClassLoader origClassLoader)
   {
      try
      {
         origClassLoader.loadClass(ProviderImpl.class.getName());
      }
      catch (Exception e)
      {
         //[JBWS-3223] On AS7 the TCCL that's set for basic (non-ws-endpoint) servlet/ejb3
         //apps doesn't have visibility on any WS implementation class, nor on any class
         //coming from dependencies provided in the ws modules only. This means for instance
         //the JAXBContext is not going to find a context impl, etc.
         //In general, we need to change the TCCL using the classloader that has been used
         //to load this javax.xml.ws.spi.Provider impl, which is the jaxws-client module.
         ClassLoader clientClassLoader = ProviderImpl.class.getClassLoader();
         
         //first ensure the default bus is loaded through the client classloader only
         //(no deployment classloader contribution)
         if (BusFactory.getDefaultBus(false) == null)
         {
            JBossWSBusFactory.getDefaultBus(clientClassLoader);
         }
         //then setup a new TCCL having visibility over both the client path (JBossWS
         //jaxws-client module on AS7) and the the former TCCL (i.e. the deployment classloader)
         setContextClassLoader(createDelegateClassLoader(clientClassLoader, origClassLoader));
         return true;
      }
      return false;
   }
   
   static Bus setValidThreadDefaultBus()
   {
      //we need to prevent using the default bus when the current
      //thread is not already associated to a bus. In those situations we create
      //a new bus from scratch instead and link that to the thread.
      Bus bus = BusFactory.getThreadDefaultBus(false);
      if (bus == null)
      {
         bus = BusFactory.newInstance().createBus(); //this also set thread local bus internally as it's not set yet 
      }
      return bus;
   }
   
   private static DelegateClassLoader createDelegateClassLoader(final ClassLoader clientClassLoader, final ClassLoader origClassLoader)
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm == null)
      {
         return new DelegateClassLoader(clientClassLoader, origClassLoader);
      }
      else
      {
         return AccessController.doPrivileged(new PrivilegedAction<DelegateClassLoader>()
         {
            public DelegateClassLoader run()
            {
               return new DelegateClassLoader(clientClassLoader, origClassLoader);
            }
         });
      }
   }

   /**
    * Get context classloader.
    *
    * @return the current context classloader
    */
   static ClassLoader getContextClassLoader()
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm == null)
      {
         return Thread.currentThread().getContextClassLoader();
      }
      else
      {
         return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>()
         {
            public ClassLoader run()
            {
               return Thread.currentThread().getContextClassLoader();
            }
         });
      }
   }
   
   /**
    * Set context classloader.
    *
    * @param classLoader the classloader
    */
   static void setContextClassLoader(final ClassLoader classLoader)
   {
      if (System.getSecurityManager() == null)
      {
         Thread.currentThread().setContextClassLoader(classLoader);
      }
      else
      {
         AccessController.doPrivileged(new PrivilegedAction<Object>()
         {
            public Object run()
            {
               Thread.currentThread().setContextClassLoader(classLoader);
               return null;
            }
         });
      }
   }
   
   /**
    * A javax.xml.ws.Endpoint implementation delegating to a provided one
    * that sets the TCCL before doing publish.
    * 
    */
   static final class DelegateEndpointImpl extends Endpoint
   {
      private Endpoint delegate;
      
      public DelegateEndpointImpl(Endpoint delegate)
      {
         this.delegate = delegate;
      }
      
      @Override
      public Binding getBinding()
      {
         return delegate.getBinding();
      }

      @Override
      public Object getImplementor()
      {
         return delegate.getImplementor();
      }

      @Override
      public void publish(String address)
      {
         ClassLoader origClassLoader = getContextClassLoader();
         boolean restoreTCCL = false;
         try
         {
            restoreTCCL = checkAndFixContextClassLoader(origClassLoader);
            delegate.publish(address);
         }
         finally
         {
            if (restoreTCCL)
               setContextClassLoader(origClassLoader);
         }
      }

      @Override
      public void publish(Object serverContext)
      {
         ClassLoader origClassLoader = getContextClassLoader();
         boolean restoreTCCL = false;
         try
         {
            restoreTCCL = checkAndFixContextClassLoader(origClassLoader);
            delegate.publish(serverContext);
         }
         finally
         {
            if (restoreTCCL)
               setContextClassLoader(origClassLoader);
         }
      }

      @Override
      public void stop()
      {
         delegate.stop();
      }

      @Override
      public boolean isPublished()
      {
         return delegate.isPublished();
      }

      @Override
      public List<Source> getMetadata()
      {
         return delegate.getMetadata();
      }

      @Override
      public void setMetadata(List<Source> metadata)
      {
         delegate.setMetadata(metadata);
      }

      @Override
      public Executor getExecutor()
      {
         return delegate.getExecutor();
      }

      @Override
      public void setExecutor(Executor executor)
      {
         delegate.setExecutor(executor);
      }

      @Override
      public Map<String, Object> getProperties()
      {
         return delegate.getProperties();
      }

      @Override
      public void setProperties(Map<String, Object> properties)
      {
         delegate.setProperties(properties);
      }

      @Override
      public EndpointReference getEndpointReference(Element... referenceParameters)
      {
         ClassLoader origClassLoader = getContextClassLoader();
         boolean restoreTCCL = false;
         try
         {
            restoreTCCL = checkAndFixContextClassLoader(origClassLoader);
            return delegate.getEndpointReference(referenceParameters);
         }
         finally
         {
            if (restoreTCCL)
               setContextClassLoader(origClassLoader);
         }
      }

      @Override
      public <T extends EndpointReference> T getEndpointReference(Class<T> clazz, Element... referenceParameters)
      {
         ClassLoader origClassLoader = getContextClassLoader();
         boolean restoreTCCL = false;
         try
         {
            restoreTCCL = checkAndFixContextClassLoader(origClassLoader);
            return delegate.getEndpointReference(clazz, referenceParameters);
         }
         finally
         {
            if (restoreTCCL)
               setContextClassLoader(origClassLoader);
         }
      }

      @Override
      //jaxws2.2 api
      public void setEndpointContext(EndpointContext ctxt)
      {
         delegate.setEndpointContext(ctxt);
      }
      
      @Override
      //jaxws2.2 api
      public void publish(javax.xml.ws.spi.http.HttpContext context)
      {
         delegate.publish(context);
      }
   }
   
   /**
    * An extension of the org.apache.cxf.jaxws.ServiceImpl allowing for
    * setting JBossWS client default config handlers.
    *
    */
   static final class JBossWSServiceImpl extends ServiceImpl {
      
      public JBossWSServiceImpl(Bus b, URL url, QName name, Class<?> cls, WebServiceFeature ... f) {
         super(b, url, name, cls, f);
      }
      
      @Override
      protected <T> T createPort(QName portName, EndpointReferenceType epr, Class<T> serviceEndpointInterface, 
            WebServiceFeature... features) {
         T port = super.createPort(portName, epr, serviceEndpointInterface, features);
         setupClient(port, features);
         return port;
      }
      
      @Override
      public <T> Dispatch<T> createDispatch(QName portName,
            Class<T> type,
            JAXBContext context,
            Mode mode,
            WebServiceFeature... features) {
         Dispatch<T> dispatch = super.createDispatch(portName, type, context, mode, features);
         setupClient(dispatch, features);
         return dispatch;
      }
      
      protected void setupClient(Object obj, WebServiceFeature... features) {
         Binding binding = ((BindingProvider)obj).getBinding();
         Client client = obj instanceof DispatchImpl<?> ? ((DispatchImpl<?>)obj).getClient() : ClientProxy.getClient(obj);
         client.getOutInterceptors().add(new HandlerChainSortInterceptor(binding));
         if (jbossModulesEnv) { //optimization for avoiding checking for a server config when we know for sure we're out-of-container
            ServerConfig sc = getServerConfig();
            if (sc != null) {
               for (ClientConfig config : sc.getClientConfigs()) {
                  if (config.getConfigName().equals(ClientConfig.STANDARD_CLIENT_CONFIG)) {
                     CXFClientConfigurer helper = new CXFClientConfigurer();
                     helper.setupConfigHandlers(binding, config);
                     helper.setConfigProperties(client, config.getProperties());
                  }
               }
            }
         }
         if (features != null) {
            for (WebServiceFeature f : features) {
               if (f instanceof AbstractClientFeature) {
                  ((AbstractClientFeature)f).initialize(obj);
               }
            }
         }
      }
   }
   
   //lazy get the server config (and try once per classloader only)
   private static synchronized ServerConfig getServerConfig()
   {
      if (!serverConfigInit)
      {
         try {
            final ClassLoader cl = ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader();
            serverConfig = SPIProvider.getInstance().getSPI(ServerConfigFactory.class, cl).getServerConfig();
         } catch (Exception e) {
            Loggers.ROOT_LOGGER.cannotRetrieveServerConfigIgnoreForClients(e);
         } finally {
            serverConfigInit = true;
         }
      }
      return serverConfig;
   }

}
