/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf.deployment;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.jaxws.support.JaxWsImplementorInfo;
import org.apache.cxf.service.Service;
import org.jboss.logging.Logger;
import org.jboss.ws.api.util.BundleUtils;
import org.jboss.ws.common.utils.DelegateClassLoader;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.spi.metadata.config.CommonConfig;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerChainMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerMetaData;

/**
 * An extension of @see org.apache.cxf.jaxws.EndpointImpl for dealing with
 * JBossWS integration needs. 
 * 
 * @author alessio.soldano@jboss.com
 * @since 01-Apr-2010
 *
 */
public class EndpointImpl extends org.apache.cxf.jaxws22.EndpointImpl
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(EndpointImpl.class);
   private WSDLFilePublisher wsdlPublisher;
   private CommonConfig config;
   
   private static Map<String, String> bindingIDs = new HashMap<String, String>();
   static {
      bindingIDs.put(SOAPBinding.SOAP11HTTP_BINDING, "##SOAP11_HTTP");
      bindingIDs.put(SOAPBinding.SOAP12HTTP_BINDING, "##SOAP12_HTTP");
      bindingIDs.put(SOAPBinding.SOAP11HTTP_MTOM_BINDING, "##SOAP11_HTTP_MTOM");
      bindingIDs.put(SOAPBinding.SOAP12HTTP_MTOM_BINDING, "##SOAP12_HTTP_MTOM");
      bindingIDs.put(HTTPBinding.HTTP_BINDING, "##XML_HTTP");
   }

   public EndpointImpl(Object implementor)
   {
      super(implementor);
   }
   
   public EndpointImpl(Bus bus, Object implementor)
   {
      super(bus, implementor);
   }

   @Override
   protected void doPublish(String addr)
   {
      super.getServerFactory().setBlockPostConstruct(true);
      super.doPublish(addr);
      //allow for configuration so that the wsdlPublisher can be set be the JBossWSCXFConfigurer
      configureObject(this);
      setupConfigHandlers();
      //publish the wsdl to data/wsdl
      publishContractToFilesystem();
   }

   /**
    * Sets the JAXWS endpoint config for the current endpoint. This is called by configurer when
    * org.apache.cxf.jaxws.EndpointImpl#getServer(..) executes 'configureObject(this)'
    * 
    */
   public void setEndpointConfig(CommonConfig config)
   {
      if (this.config == null)
      {
         this.config = config;
         //setup using provided configuration
         Map<String, String> epConfProps = config.getProperties();
         if (!epConfProps.isEmpty())
         {
            if (getProperties() == null)
            {
               Map<String, Object> props = new HashMap<String, Object>();
               props.putAll(epConfProps);
               setProperties(props);
            }
            else
            {
               getProperties().putAll(epConfProps);
            }
         }
         //handlers config is done later, as when this methods is called getBinding() can't
         //be used without messing with the servlet destinations due to the endpoint address
         //not having been rewritten yet.
      }
   }
   
   @SuppressWarnings("rawtypes")
   protected List<Handler> convertToHandlers(List<UnifiedHandlerChainMetaData> handlerChains)
   {
      List<Handler> handlers = new LinkedList<Handler>();
      if (handlerChains != null && !handlerChains.isEmpty())
      {
         final String protocolBinding = bindingIDs.get(getBinding().getBindingID());
         for (UnifiedHandlerChainMetaData handlerChain : handlerChains)
         {
            if (handlerChain.getPortNamePattern() != null || handlerChain.getServiceNamePattern() != null)
            {
               Logger.getLogger(this.getClass()).warn(BundleUtils.getMessage(bundle, "FILTERS_NOT_SUPPORTED"));
            }
            if (matchProtocolBinding(protocolBinding, handlerChain.getProtocolBindings())) {
               for (UnifiedHandlerMetaData uhmd : handlerChain.getHandlers())
               {
                  if (uhmd.getInitParams() != null && !uhmd.getInitParams().isEmpty())
                  {
                     Logger.getLogger(this.getClass()).warn(BundleUtils.getMessage(bundle, "INIT_PARAMS_NOT_SUPPORTED"));
                  }
                  Object h = newInstance(uhmd.getHandlerClass());
                  if (h != null)
                  {
                     if (h instanceof Handler)
                     {
                        handlers.add((Handler)h);
                     }
                     else
                     {
                        throw new RuntimeException(BundleUtils.getMessage(bundle, "NOT_HANDLER_INSTANCE", h));
                     }
                  }
               }
            }
         }
      }
      return handlers;
   }
   
   private static boolean matchProtocolBinding(String currentProtocolBinding, String handlerChainProtocolBindings) {
      if (handlerChainProtocolBindings == null)
         return true;
      List<String> protocolBindings = new LinkedList<String>();
      if (handlerChainProtocolBindings != null) {
         StringTokenizer st = new StringTokenizer(handlerChainProtocolBindings, " ", false);
         while (st.hasMoreTokens()) {
            protocolBindings.add(st.nextToken());
         }
      }
      return protocolBindings.contains(currentProtocolBinding);
   }
   
   private static Object newInstance(String className)
   {
      try
      {
         ClassLoader loader = new DelegateClassLoader(ClassLoaderProvider.getDefaultProvider()
               .getServerIntegrationClassLoader(), SecurityActions.getContextClassLoader());
         Class<?> clazz = loader.loadClass(className);
         return clazz.newInstance();
      }
      catch (Exception e)
      {
         Logger.getLogger(EndpointImpl.class).warnf(e, BundleUtils.getMessage(bundle, "CAN_NOT_ADD_HANDLER" , className));
         return null;
      }
   }
   
   @SuppressWarnings("rawtypes")
   protected void setupConfigHandlers()
   {
      if (config != null) {
         List<Handler> handlers = convertToHandlers(config.getPreHandlerChains()); //PRE
         handlers.addAll(getBinding().getHandlerChain()); //ENDPOINT
         handlers.addAll(convertToHandlers(config.getPostHandlerChains())); //POST
         getBinding().setHandlerChain(handlers);
      }
   }


   /**
    * Publish the contract to a file using the configured wsdl publisher
    * 
    * @param endpoint
    */
   protected void publishContractToFilesystem()
   {
      // Publish wsdl after endpoint deployment, as required by JSR-109, section 8.2
      if (wsdlPublisher != null)
      {
         Endpoint endpoint = getServer().getEndpoint();
         Service service = endpoint.getService();
         try
         {
            JaxWsImplementorInfo info = new JaxWsImplementorInfo(getImplementorClass());
            wsdlPublisher.publishWsdlFiles(service.getName(), info.getWsdlLocation(), BusFactory.getThreadDefaultBus(false), service.getServiceInfos());
         }
         catch (IOException ioe)
         {
            throw new RuntimeException(BundleUtils.getMessage(bundle, "PUBLISHING_WSDL_ERROR",  service.getName()),  ioe);
         }
      }
      else
      {
         Logger.getLogger(this.getClass()).warn(BundleUtils.getMessage(bundle, "UNABLE_TO_PUBLISH_CONTRACT"));
      }
   }

   private void configureObject(Object instance)
   {
      Configurer configurer = getBus().getExtension(Configurer.class);
      if (null != configurer)
      {
         configurer.configureBean(instance);
      }
   }

   @Override
   public String getBeanName()
   {
      QName endpointName = this.getEndpointName();
      if (endpointName == null)
      {
         JaxWsImplementorInfo implInfo = new JaxWsImplementorInfo(getImplementorClass());
         endpointName = implInfo.getEndpointName();
      }
      return endpointName.toString() + ".jaxws-endpoint";
   }

   public WSDLFilePublisher getWsdlPublisher()
   {
      return wsdlPublisher;
   }

   public void setWsdlPublisher(WSDLFilePublisher wsdlPublisher)
   {
      this.wsdlPublisher = wsdlPublisher;
   }

}
