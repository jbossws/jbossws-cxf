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
package org.jboss.wsf.stack.cxf.client.configuration;

import org.apache.cxf.databinding.DataBinding;
import org.apache.cxf.frontend.AbstractWSDLBasedEndpointFactory;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.wsdl.service.factory.ReflectionServiceFactoryBean;
import org.jboss.ws.api.binding.BindingCustomization;
import org.jboss.ws.api.binding.JAXBBindingCustomization;

/**
 * 
 * @author alessio.soldano@jboss.com
 * @since 16-Jun-2010
 */
public class BeanCustomizer
{
   protected BindingCustomization customization;

   public void customize(Object beanInstance)
   {
      if (beanInstance instanceof AbstractWSDLBasedEndpointFactory)
      {
         configureEndpointFactory((AbstractWSDLBasedEndpointFactory)beanInstance);
      }
      else if (beanInstance instanceof ClientProxyFactoryBean)
      {
         configureClientProxyFactoryBean((ClientProxyFactoryBean)beanInstance);
      }
      //add other beans configuration here below
   }
   
   /**
    * Configure the endpoint factory
    * 
    * @param factory
    */
   protected void configureEndpointFactory(AbstractWSDLBasedEndpointFactory factory)
   {
      //Configure binding customization
      if (customization != null)
      {
         ReflectionServiceFactoryBean serviceFactory = factory.getServiceFactory();
         //customize default databinding (early pulls in ServiceFactory default databinding and configure it, as it's lazily loaded)
         serviceFactory.reset();
         DataBinding serviceFactoryDataBinding = serviceFactory.getDataBinding(true);
         configureBindingCustomization(serviceFactoryDataBinding, customization);
         serviceFactory.setDataBinding(serviceFactoryDataBinding);
         //customize user provided databinding (CXF later overrides the ServiceFactory databinding using the user provided one) 
         if (factory.getDataBinding() == null)
         {
            //set the endpoint factory's databinding to prevent CXF resetting everything because user did not provide anything
            factory.setDataBinding(serviceFactoryDataBinding);
         }
         else
         {
            configureBindingCustomization(factory.getDataBinding(), customization);
         }
      }
      //add other configurations here below
   }
   
   /**
    * Configure the client proxy factory; currently set the binding customization in the databinding (Client Side).
    * 
    * @param factory
    */
   protected void configureClientProxyFactoryBean(ClientProxyFactoryBean factory)
   {
      //Configure binding customization
      if (customization != null)
      {
         //customize default databinding (early pulls in ServiceFactory default databinding and configure it, as it's lazily loaded)
         ReflectionServiceFactoryBean serviceFactory = factory.getServiceFactory();
         serviceFactory.reset();
         DataBinding serviceFactoryDataBinding = serviceFactory.getDataBinding(true);
         configureBindingCustomization(serviceFactoryDataBinding, customization);
         serviceFactory.setDataBinding(serviceFactoryDataBinding);
         //customize user provided databinding (CXF later overrides the ServiceFactory databinding using the user provided one) 
         if (factory.getDataBinding() == null)
         {
            //set the endpoint factory's databinding to prevent CXF resetting everything because user did not provide anything
            factory.setDataBinding(serviceFactoryDataBinding);
         }
         else
         {
            configureBindingCustomization(factory.getDataBinding(), customization);
         }
      }
      //add other configurations here below
   }
   
   @SuppressWarnings("unchecked")
   protected static void configureBindingCustomization(DataBinding db, BindingCustomization customization)
   {
      //JAXB
      if (customization instanceof JAXBBindingCustomization)
      {
         if (db != null && db instanceof JAXBDataBinding)
         {
            ((JAXBDataBinding)db).setContextProperties(customization);
         }
      }
      //add other binding customizations here below
   }


   public void setBindingCustomization(BindingCustomization customization)
   {
      this.customization = customization;
   }
}
