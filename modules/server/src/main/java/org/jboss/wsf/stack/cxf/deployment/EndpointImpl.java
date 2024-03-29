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
package org.jboss.wsf.stack.cxf.deployment;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.cxf.Bus;
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.frontend.WSDLGetUtils;
import org.apache.cxf.jaxws.support.JaxWsImplementorInfo;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.jboss.ws.common.configuration.ConfigHelper;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.spi.metadata.config.CommonConfig;
import org.jboss.wsf.spi.metadata.config.SOAPAddressRewriteMetadata;
import org.jboss.wsf.stack.cxf.addressRewrite.SoapAddressRewriteHelper;
import org.jboss.wsf.stack.cxf.client.configuration.FeatureUtils;
import org.jboss.wsf.stack.cxf.client.configuration.InterceptorUtils;
import org.jboss.wsf.stack.cxf.client.configuration.PropertyReferenceUtils;
import org.jboss.wsf.stack.cxf.i18n.Loggers;


/**
 * An extension of @see org.apache.cxf.jaxws.EndpointImpl for dealing with
 * JBossWS integration needs. 
 * 
 * @author alessio.soldano@jboss.com
 * @since 01-Apr-2010
 *
 */
public class EndpointImpl extends org.apache.cxf.jaxws.EndpointImpl
{
   private WSDLFilePublisher wsdlPublisher;
   private CommonConfig config;
   private SOAPAddressRewriteMetadata sarm;
   
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
            final Map<String, Object> propMap = getProperties();
            if (propMap == null)
            {
               setProperties(new HashMap<String, Object>(epConfProps));
            }
            else
            {
               propMap.putAll(epConfProps);
            }
            InterceptorUtils.addInterceptors(this, epConfProps);
            FeatureUtils.addFeatures(this, getBus(), epConfProps);
            PropertyReferenceUtils.createPropertyReference(epConfProps, this.getProperties());
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
            String wsdlLocation = getWsdlLocation();
            if (wsdlLocation == null) {
               JaxWsImplementorInfo info = new JaxWsImplementorInfo(getImplementorClass());
               wsdlLocation = info.getWsdlLocation();
            }
            updateSoapAddress();
            wsdlPublisher.publishWsdlFiles(service.getName(), wsdlLocation, this.getBus(), service.getServiceInfos());
         }
         catch (IOException ioe)
         {
            throw new RuntimeException(ioe);
         }
      }
      else
      {
         Loggers.DEPLOYMENT_LOGGER.unableToPublishContractDueToMissingPublisher(getImplementorClass());
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
   
   public void setSOAPAddressRewriteMetadata(SOAPAddressRewriteMetadata sarm)
   {
      this.sarm = sarm;
   }
   
   private SOAPAddressRewriteMetadata getSOAPAddressRewriteMetadata()
   {
      if (sarm == null) {
         Deployment dep = (Deployment)getBus().getProperty(Deployment.class.getName());
         sarm = dep.getAttachment(SOAPAddressRewriteMetadata.class);
      }
      return sarm;
   }

   /**
    * For both code-first and wsdl-first scenarios, reset the endpoint address
    * so that it is written to the generated wsdl file.
    */
   private void updateSoapAddress() {
      final SOAPAddressRewriteMetadata metadata = getSOAPAddressRewriteMetadata();
      if (metadata.isModifySOAPAddress()) {
         //- code-first handling
         List<ServiceInfo> sevInfos = getServer().getEndpoint().getService().getServiceInfos();
         for (ServiceInfo si: sevInfos){
            Collection<EndpointInfo > epInfos = si.getEndpoints();
            for(EndpointInfo ei: epInfos){
               String publishedEndpointUrl = (String)ei.getProperty(WSDLGetUtils.PUBLISHED_ENDPOINT_URL);
               if (publishedEndpointUrl != null){
                  ei.setAddress(publishedEndpointUrl);
               } else {
                  //- wsdl-first handling
                  if (ei.getAddress().contains(ServerConfig.UNDEFINED_HOSTNAME)) {
                     String epurl = SoapAddressRewriteHelper.getRewrittenPublishedEndpointUrl(ei.getAddress(), metadata);
                     ei.setAddress(epurl);
                  }
               }
            }
         }
      }
   }
}
