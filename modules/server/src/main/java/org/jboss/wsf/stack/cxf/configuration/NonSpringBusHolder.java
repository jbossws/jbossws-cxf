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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.resource.ResourceResolver;
import org.apache.cxf.service.invoker.Invoker;
import org.apache.cxf.transport.http.HttpDestinationFactory;
import org.apache.cxf.transport.servlet.ServletDestinationFactory;
import org.apache.cxf.ws.addressing.WSAddressingFeature;
import org.apache.cxf.ws.rm.RMManager;
import org.jboss.ws.api.binding.BindingCustomization;
import org.jboss.wsf.spi.deployment.ArchiveDeployment;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.metadata.config.SOAPAddressRewriteMetadata;
import org.jboss.wsf.spi.metadata.webservices.JBossWebservicesMetaData;
import org.jboss.wsf.stack.cxf.Messages;
import org.jboss.wsf.stack.cxf.client.configuration.JBossWSBusFactory;
import org.jboss.wsf.stack.cxf.client.configuration.JBossWSNonSpringConfigurer;
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
   protected List<EndpointImpl> endpoints = new LinkedList<EndpointImpl>();

   public NonSpringBusHolder(DDBeans metadata)
   {
      super();
      this.metadata = metadata;
      bus = new JBossWSBusFactory().createBus();
      //Force servlet transport to prevent CXF from using Jetty / http server or other transports
      bus.setExtension(new ServletDestinationFactory(), HttpDestinationFactory.class);
   }

   /**
    * Update the Bus held by the this instance using the provided parameters.
    * This basically prepares the bus for being used with JBossWS.
    * 
    * @param resolver               The ResourceResolver to configure, if any
    * @param configurer             The JBossWSCXFConfigurer to install in the bus, if any
    * @param wsmd                   The current JBossWebservicesMetaData, if any
    * @param dep                    The current deployment
    */
   @Override
   public void configure(ResourceResolver resolver, Configurer configurer, JBossWebservicesMetaData wsmd, Deployment dep)
   {
      if (configured)
      {
         throw Messages.MESSAGES.busAlreadyConfigured(bus);
      }
      super.configure(resolver, configurer, wsmd, dep);

      for (DDEndpoint dde : metadata.getEndpoints())
      {
         EndpointImpl endpoint = new EndpointImpl(bus, newInstance(dde.getImplementor()));
         if (dde.getInvoker() != null)
            endpoint.setInvoker((Invoker) newInstance(dde.getInvoker()));
         endpoint.setAddress(dde.getAddress());
         endpoint.setEndpointName(dde.getPortName());
         endpoint.setServiceName(dde.getServiceName());
         endpoint.setWsdlLocation(dde.getWsdlLocation());
         setHandlers(endpoint, dde);
         if (dde.getProperties() != null)
         {
            Map<String, Object> props = new HashMap<String, Object>();
            props.putAll(dde.getProperties());
            endpoint.setProperties(props);
         }
         if (dde.isAddressingEnabled()) 
         {
            WSAddressingFeature addressingFeature = new WSAddressingFeature();
            addressingFeature.setAddressingRequired(dde.isAddressingRequired());
            addressingFeature.setResponses(dde.getAddressingResponses());
            endpoint.getFeatures().add(addressingFeature);
         }
         endpoint.setPublishedEndpointUrl(dde.getPublishedEndpointUrl());
         endpoint.setSOAPAddressRewriteMetadata(dep.getAttachment(SOAPAddressRewriteMetadata.class));
         endpoint.publish();
         endpoints.add(endpoint);
         if (dde.isMtomEnabled())
         {
            SOAPBinding binding = (SOAPBinding) endpoint.getBinding();
            binding.setMTOMEnabled(true);
         }
      }
      configured = true;
   }
   
   @SuppressWarnings("rawtypes")
   private static void setHandlers(EndpointImpl endpoint, DDEndpoint dde)
   {
      List<String> handlers = dde.getHandlers();
      if (handlers != null && !handlers.isEmpty())
      {
         List<Handler> handlerInstances = new LinkedList<Handler>();
         for (String handler : handlers)
         {
            handlerInstances.add((Handler) newInstance(handler));
         }
         endpoint.setHandlers(handlerInstances);
      }
   }
   
   @Override
   public void close()
   {
      //Move this stuff to the bus (our own impl)?
      RMManager rmManager = bus.getExtension(RMManager.class);
      if (rmManager != null)
      {
         rmManager.shutdown();
      }
      
      for (EndpointImpl endpoint : endpoints)
      {
         if (endpoint.isPublished())
         {
            endpoint.stop();
         }
      }
      endpoints.clear();
      
      super.close();
   }

   private static Object newInstance(String className)
   {
      try
      {
         Class<?> clazz = SecurityActions.getContextClassLoader().loadClass(className);
         return clazz.newInstance();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   @Override
   public Configurer createServerConfigurer(BindingCustomization customization, WSDLFilePublisher wsdlPublisher, ArchiveDeployment dep)
   {
      ServerBeanCustomizer customizer = new ServerBeanCustomizer();
      customizer.setBindingCustomization(customization);
      customizer.setWsdlPublisher(wsdlPublisher);
      customizer.setDeployment(dep);
      return new JBossWSNonSpringConfigurer(customizer);
   }

}
