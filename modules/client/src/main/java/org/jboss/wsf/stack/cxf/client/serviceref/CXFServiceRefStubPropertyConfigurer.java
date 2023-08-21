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
package org.jboss.wsf.stack.cxf.client.serviceref;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import jakarta.xml.ws.WebServiceFeature;
import jakarta.xml.ws.soap.AddressingFeature;
import jakarta.xml.ws.soap.MTOMFeature;

import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.jaxws.support.JaxWsServiceFactoryBean;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedPortComponentRefMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedServiceRefMetaData;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedStubPropertyMetaData;
import org.jboss.wsf.stack.cxf.i18n.Loggers;

/**
 * A CXF configurer that sets the serviceref data in the JaxWsProxyFactoryBean
 *
 * @author alessio.soldano@jboss.com
 * @since 21-Jul-2009
 */
final class CXFServiceRefStubPropertyConfigurer implements Configurer
{
   private final UnifiedServiceRefMetaData serviceRefMD;
   private final Configurer delegate;

   public CXFServiceRefStubPropertyConfigurer(UnifiedServiceRefMetaData serviceRefMD, Configurer delegate)
   {
      this.serviceRefMD = serviceRefMD;
      this.delegate = delegate;
   }

   public void configureBean(Object beanInstance)
   {
      if (delegate != null)
      {
         delegate.configureBean(beanInstance);
      }
   }

   public void configureBean(String name, Object beanInstance)
   {
      if (name != null && beanInstance instanceof JaxWsProxyFactoryBean)
      {
         QName portQName = null;
         try
         {
            String portName = name.substring(0, name.indexOf(".jaxws-client.proxyFactory"));
            portQName = QName.valueOf(portName);
         }
         catch (Exception e)
         {
            Loggers.ROOT_LOGGER.cannotRetrievePortQNameTryingMatchingUsingEpInterface(name, e);
         }
         configureJaxWsProxyFactoryBean(portQName, (JaxWsProxyFactoryBean)beanInstance);
      }
      if (delegate != null)
      {
         delegate.configureBean(name, beanInstance);
      }
   }
   
   private synchronized void configureJaxWsProxyFactoryBean(QName portQName, JaxWsProxyFactoryBean proxyFactory)
   {
      Class<?> clazz = proxyFactory.getServiceClass();
      UnifiedPortComponentRefMetaData upcmd = serviceRefMD.getPortComponentRef(clazz != null ? clazz.getName() : null, portQName);
      if (upcmd != null)
      {
         setProperties(proxyFactory, upcmd);
         setWSFeature((JaxWsServiceFactoryBean)proxyFactory.getServiceFactory(), upcmd);
      }
   }
   
   private void setWSFeature(JaxWsServiceFactoryBean serviceFactoryBean, UnifiedPortComponentRefMetaData upcmd)
   {
      List<WebServiceFeature> features = new LinkedList<WebServiceFeature>();
      List<WebServiceFeature> prevFeatures = serviceFactoryBean.getWsFeatures();
      if (prevFeatures != null)
      {
         features.addAll(prevFeatures);
      }

      if (upcmd.isMtomEnabled())
      {
         if (upcmd.getMtomThreshold() > 0) 
         {
            features.add(new MTOMFeature(true, upcmd.getMtomThreshold()));
         } 
         else 
         {
            features.add(new MTOMFeature(true));
         }
      }
      
      if (upcmd.isAddressingEnabled())
      {
         final String refResponses = upcmd.getAddressingResponses();
         AddressingFeature.Responses responses = AddressingFeature.Responses.ALL;
         if ("ANONYMOUS".equals(refResponses))
            responses = AddressingFeature.Responses.ANONYMOUS;
         if ("NON_ANONYMOUS".equals(refResponses))
            responses = AddressingFeature.Responses.NON_ANONYMOUS;
         features.add(new AddressingFeature(true, upcmd.isAddressingRequired(), responses));
      }
      
      serviceFactoryBean.setWsFeatures(features);
      
   }
   
   private void setProperties(JaxWsProxyFactoryBean proxyFactory, UnifiedPortComponentRefMetaData upcmd)
   {
      Map<String, Object> properties = proxyFactory.getProperties();
      if (properties == null)
      {
         properties = new HashMap<String, Object>();
         proxyFactory.setProperties(properties);
      }
      for (UnifiedStubPropertyMetaData prop : upcmd.getStubProperties())
      {
         properties.put(prop.getPropName(), prop.getPropValue());
      }
   }
}
