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
import java.util.Map;
import java.util.ResourceBundle;

import javax.xml.namespace.QName;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.jaxws.support.JaxWsImplementorInfo;
import org.apache.cxf.service.Service;
import org.jboss.logging.Logger;
import org.jboss.ws.api.util.BundleUtils;
import org.jboss.ws.common.configuration.ConfigHelper;
import org.jboss.wsf.spi.metadata.config.CommonConfig;

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
   
   protected void setupConfigHandlers()
   {
      if (config != null) {
         ConfigHelper helper = new ConfigHelper();
         helper.setupConfigHandlers(getBinding(), config);
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
