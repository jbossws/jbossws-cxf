/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf.client.configuration;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.databinding.DataBinding;
import org.apache.cxf.frontend.AbstractWSDLBasedEndpointFactory;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.apache.cxf.wsdl.service.factory.ReflectionServiceFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
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
      else if (beanInstance instanceof HTTPConduit)
      {
         configureHTTPConduit((HTTPConduit)beanInstance);
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
   
   /**
    * Configure the HTTPConduit; currently allows for setting disableCNcheck in TLS client parameters according
    * to the JBoss' org.jboss.security.ignoreHttpsHost system property.
    * 
    * @param conduit
    */
   protected void configureHTTPConduit(HTTPConduit conduit)
   {
      TLSClientParameters parameters = conduit.getTlsClientParameters();
      if (parameters == null) //don't do anything when user already provided a configuration
      {
         parameters = new TLSClientParameters();
         parameters.setUseHttpsURLConnectionDefaultSslSocketFactory(true);
         if (SecurityActions.getBoolean("org.jboss.security.ignoreHttpsHost"))
         {
            parameters.setDisableCNCheck(true);
         }
         conduit.setTlsClientParameters(parameters);
      }
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
