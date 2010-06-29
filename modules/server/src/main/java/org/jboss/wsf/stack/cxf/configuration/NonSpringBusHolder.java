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

import java.io.IOException;

import javax.xml.ws.soap.SOAPBinding;

import org.apache.cxf.binding.soap.SoapTransportFactory;
import org.apache.cxf.bus.extension.ExtensionManager;
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.resource.ResourceResolver;
import org.apache.cxf.service.invoker.Invoker;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.cxf.transport.servlet.ServletTransportFactory;
import org.jboss.wsf.spi.binding.BindingCustomization;
import org.jboss.wsf.stack.cxf.client.configuration.JBossWSConfigurer;
import org.jboss.wsf.stack.cxf.client.configuration.JBossWSNonSpringBusFactory;
import org.jboss.wsf.stack.cxf.deployment.EndpointImpl;
import org.jboss.wsf.stack.cxf.deployment.WSDLFilePublisher;
import org.jboss.wsf.stack.cxf.metadata.services.DDBeans;
import org.jboss.wsf.stack.cxf.metadata.services.DDEndpoint;

/**
 * A @see{org.jboss.wsf.stack.cxf.configuration.BusHolder} that
 * does not use any Spring facilities.
 * 
 * @author alessio.soldano@jboss.com
 * @since 16-Jun-2010
 *
 */
public class NonSpringBusHolder extends BusHolder
{
   private boolean configured = false;
   protected DDBeans metadata;
   
   public NonSpringBusHolder(DDBeans metadata)
   {
      super();
      this.metadata = metadata;
      bus = new JBossWSNonSpringBusFactory().createBus();
      //Force servlet transport to prevent CXF from using Jetty as a transport
      ExtensionManager em = bus.getExtension(ExtensionManager.class);
      em.activateAllByType(ConduitInitiator.class); //need to activate/register all the beans implementing ConduitInitiator so that does not happen later
      DestinationFactory factory = new ServletTransportFactory(bus);
      for (String s : factory.getTransportIds()) {
          registerTransport(factory, s);
      }
   }
   
   /**
    * Update the Bus held by the this instance using the provided parameters.
    * This basically prepares the bus for being used with JBossWS.
    * 
    * @param soapTransportFactory   The SoapTransportFactory to configure, if any
    * @param resolver               The ResourceResolver to configure, if any
    * @param configurer             The JBossWSCXFConfigurer to install in the bus, if any
    */
   @Override
   public void configure(SoapTransportFactory soapTransportFactory, ResourceResolver resolver, Configurer configurer)
   {
      if (configured)
      {
         throw new IllegalStateException("Underlying bus is already configured for JBossWS use!");
      }
      super.configure(soapTransportFactory, resolver, configurer);
      
      for (DDEndpoint dde : metadata.getEndpoints())
      {
         EndpointImpl endpoint = new EndpointImpl(bus, newInstance(dde.getImplementor()));
         endpoint.setInvoker((Invoker)newInstance(dde.getInvoker()));
         endpoint.setAddress(dde.getAddress());
         endpoint.setEndpointName(dde.getPortName());
         endpoint.setServiceName(dde.getServiceName());
         endpoint.setWsdlLocation(dde.getWsdlLocation());
         endpoint.publish();
         if (dde.isMtomEnabled())
         {
            SOAPBinding binding = (SOAPBinding) endpoint.getBinding();
            binding.setMTOMEnabled(true);
         }
         //TODO!! We need to stop the endpoint on undeployment
      }
      configured = true;
   }
   
   private static Object newInstance(String className)
   {
      try
      {
         Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
         return clazz.newInstance();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }
   
   @Override
   public Configurer createServerConfigurer(BindingCustomization customization, WSDLFilePublisher wsdlPublisher)
   {
      ServerBeanCustomizer customizer = new ServerBeanCustomizer();
      customizer.setBindingCustomization(customization);
      customizer.setWsdlPublisher(wsdlPublisher);
      return new JBossWSConfigurer(customizer);
   }

}
