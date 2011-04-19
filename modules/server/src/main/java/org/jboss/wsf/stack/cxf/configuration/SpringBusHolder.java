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
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.apache.cxf.Bus;
import org.apache.cxf.binding.soap.SoapTransportFactory;
import org.apache.cxf.bus.spring.BusApplicationContext;
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.resource.ResourceResolver;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.cxf.transport.http.HTTPTransportFactory;
import org.apache.cxf.transport.http.HttpDestinationFactory;
import org.apache.cxf.transport.servlet.ServletDestinationFactory;
//import org.apache.cxf.transport.servlet.ServletTransportFactory;
import org.apache.ws.security.WSSConfig;
import org.jboss.logging.Logger;
import org.jboss.wsf.spi.binding.BindingCustomization;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.stack.cxf.client.configuration.JBossWSSpringBusFactory;
import org.jboss.wsf.stack.cxf.deployment.WSDLFilePublisher;
import org.jboss.wsf.stack.cxf.spring.handler.NamespaceHandlerResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.InputStreamResource;

/**
 * A Spring-enabled version of @see{org.jboss.wsf.stack.cxf.configuration.BusHolder}
 * 
 * @author alessio.soldano@jboss.com
 * @since 16-Jun-2010
 *
 */
public class SpringBusHolder extends BusHolder
{
   private static final Logger log = Logger.getLogger(BusHolder.class);

   private boolean configured = false;

   protected BusApplicationContext ctx;

   protected List<GenericApplicationContext> additionalCtx = new LinkedList<GenericApplicationContext>();

   protected URL[] additionalLocations;

   public SpringBusHolder(URL location, URL... additionalLocations)
   {
      super();
      this.additionalLocations = additionalLocations;
      createBus(location);
   }

   /**
    * Creates the Bus using a SpringBusFactory with no specific Spring application context.
    * Then loads additional configurations from the provided location
    * 
    * @param location
    * @return
    */
   protected void createBus(URL location)
   {
      bus = new JBossWSSpringBusFactory().createBus();
      ctx = bus.getExtension(BusApplicationContext.class);
      //Load additional configurations from cxf-servlet.xml
      if (location != null)
      {
         try
         {
            additionalCtx.add(loadAdditionalConfig(ctx, location));
         }
         catch (IOException e)
         {
            if (log.isTraceEnabled())
               log.trace("Could not load additional config from location: " + location, e);
         }
      }
      //Force servlet transport to prevent CXF from using Jetty or other transports
      new HTTPTransportFactory(bus);
      bus.setExtension(new ServletDestinationFactory(), HttpDestinationFactory.class);
//      //Force servlet transport to prevent CXF from using Jetty or other transports
//      DestinationFactory factory = new ServletTransportFactory(bus);
//      for (String s : factory.getTransportIds())
//      {
//         registerTransport(factory, s);
//      }
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
      if (additionalLocations != null)
      {
         for (URL jbossCxfXml : additionalLocations)
         {
            try
            {
               additionalCtx.add(loadAdditionalConfig(ctx, jbossCxfXml));
            }
            catch (IOException e)
            {
               throw new RuntimeException("Unable to load configuration from " + jbossCxfXml, e);
            }
         }
      }
      //try early configuration of xmlsec engine through WSS4J to avoid doing this
      //later when the TCCL won't have visibility over the xmlsec internals
      try
      {
         WSSConfig.getNewInstance();
      }
      catch (Exception e)
      {
         log.warn("Could not early initialize security engine!");
         if (log.isTraceEnabled())
         {
            log.trace("Error while getting default WSSConfig: ", e);
         }
      }
      configured = true;
   }

   @Override
   public Configurer createServerConfigurer(BindingCustomization customization, WSDLFilePublisher wsdlPublisher,
         List<Endpoint> depEndpoints)
   {
      ApplicationContext ctx = bus.getExtension(BusApplicationContext.class);
      ServerBeanCustomizer customizer = new ServerBeanCustomizer();
      customizer.setBindingCustomization(customization);
      customizer.setWsdlPublisher(wsdlPublisher);
      customizer.setDeploymentEndpoints(depEndpoints);
      JBossWSServerSpringConfigurer serverConfigurer = new JBossWSServerSpringConfigurer(ctx);
      serverConfigurer.setCustomizer(customizer);
      return serverConfigurer;
   }

   protected static GenericApplicationContext loadAdditionalConfig(ApplicationContext ctx, URL locationUrl)
         throws IOException
   {
      if (locationUrl == null)
         throw new IllegalArgumentException("Cannot load additional config from null location!");
      InputStream is = locationUrl.openStream();
      GenericApplicationContext childCtx = new GenericApplicationContext(ctx);
      XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(childCtx);
      reader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD);
      reader.setNamespaceHandlerResolver(new NamespaceHandlerResolver(SecurityActions.getContextClassLoader()));
      reader.loadBeanDefinitions(new InputStreamResource(is));
      childCtx.refresh();
      return childCtx;
   }

   /**
    * Performs close operations (currently implies destroying additional contexts)
    * 
    */
   @Override
   public void close()
   {
      for (GenericApplicationContext gac : additionalCtx)
      {
         gac.destroy();
      }
      super.close();
   }

   @Override
   protected void setBus(Bus bus)
   {
      super.setBus(bus);
      ctx = (bus.getExtension(BusApplicationContext.class));
   }
}
