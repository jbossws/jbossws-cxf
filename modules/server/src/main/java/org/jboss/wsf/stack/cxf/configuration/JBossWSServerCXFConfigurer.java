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
package org.jboss.wsf.stack.cxf.configuration;

import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.databinding.DataBinding;
import org.apache.cxf.frontend.AbstractWSDLBasedEndpointFactory;
import org.apache.cxf.service.factory.ReflectionServiceFactoryBean;
import org.jboss.wsf.spi.binding.BindingCustomization;
import org.jboss.wsf.stack.cxf.WSDLFilePublisher;
import org.jboss.wsf.stack.cxf.client.configuration.JBossWSCXFConfigurer;
import org.jboss.wsf.stack.cxf.deployment.EndpointImpl;

/**
 * A JBossWSCXFConfigured to be used on server side
 * 
 * @author alessio.soldano@jboss.com
 * @since 31-Mar-2010
 */
public class JBossWSServerCXFConfigurer extends JBossWSCXFConfigurer
{
   private WSDLFilePublisher wsdlPublisher;

   public JBossWSServerCXFConfigurer(Configurer delegate)
   {
      super(delegate);
   }
   
   public JBossWSServerCXFConfigurer(Configurer delegate, BindingCustomization customization, WSDLFilePublisher wsdlPublisher)
   {
      super(delegate, customization);
      this.wsdlPublisher = wsdlPublisher;
   }
   
   @Override
   protected void internalConfigure(Object beanInstance)
   {
      super.internalConfigure(beanInstance);
      if (beanInstance instanceof EndpointImpl)
      {
         configureEndpoint((EndpointImpl)beanInstance);
      }
   }

   @Override
   protected synchronized void configureEndpointFactory(AbstractWSDLBasedEndpointFactory factory)
   {
      //Configure binding customization
      if (customization != null)
      {
         ReflectionServiceFactoryBean serviceFactory = factory.getServiceFactory();
         //customize default databinding (early pulls in ServiceFactory default databinding and configure it, as it's lazily loaded)
         serviceFactory.reset();
         DataBinding serviceFactoryDataBinding = serviceFactory.getDataBinding(true);
         setBindingCustomization(serviceFactoryDataBinding, customization);
         serviceFactory.setDataBinding(serviceFactoryDataBinding);
         //customize user provided databinding (CXF later overrides the ServiceFactory databinding using the user provided one) 
         if (factory.getDataBinding() == null)
         {
            //set the endpoint factory's databinding to prevent CXF resetting everything because user did not provide anything
            factory.setDataBinding(serviceFactoryDataBinding);
         }
         else
         {
            setBindingCustomization(factory.getDataBinding(), customization);
         }
      }
      //add other configurations here below
   }
   
   protected synchronized void configureEndpoint(EndpointImpl endpoint)
   {
      //Configure wsdl file publisher
      if (wsdlPublisher != null)
      {
         endpoint.setWsdlPublisher(wsdlPublisher);
      }
   }
   
   public void setWsdlPublisher(WSDLFilePublisher wsdlPublisher)
   {
      this.wsdlPublisher = wsdlPublisher;
   }

}
