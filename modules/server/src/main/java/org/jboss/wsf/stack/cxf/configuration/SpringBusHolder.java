/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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

import static org.jboss.wsf.stack.cxf.Loggers.DEPLOYMENT_LOGGER;
import static org.jboss.wsf.stack.cxf.Messages.MESSAGES;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.BusApplicationContext;
import org.apache.cxf.configuration.ConfiguredBeanLocator;
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.resource.ResourceResolver;
import org.apache.cxf.transport.http.HttpDestinationFactory;
import org.apache.cxf.transport.servlet.ServletDestinationFactory;
import org.jboss.ws.api.binding.BindingCustomization;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.UnifiedVirtualFile;
import org.jboss.wsf.spi.metadata.webservices.JBossWebservicesMetaData;
import org.jboss.wsf.stack.cxf.client.configuration.JBossWSSpringBusFactory;
import org.jboss.wsf.stack.cxf.client.configuration.JBossWSSpringConfigurer;
import org.jboss.wsf.stack.cxf.deployment.WSDLFilePublisher;
import org.jboss.wsf.stack.cxf.spring.handler.NamespaceHandlerResolver;
import org.jboss.wsf.stack.cxf.spring.parser.JaxwsEndpointDefinitionParser.JBossWSSpringEndpointImpl;
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
   private boolean configured = false;
   
   protected BusApplicationContext ctx;
   protected List<GenericApplicationContext> additionalCtx = new LinkedList<GenericApplicationContext>();
   
   private ConfiguredBeanLocator delegatingBeanLocator;

   protected URL jbosswsCxfLocation;
   protected URL[] additionalLocations;

   public SpringBusHolder(URL location, URL jbosswscxf, URL... additionalLocations)
   {
      super();
      createBus(location);
      jbosswsCxfLocation = jbosswscxf;
      this.additionalLocations = additionalLocations;
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
      //use createBus(URL[] urls) method instead of createBus() to bypass
      //the cxf/jbossws-cxf check on actual need for spring bus (we know
      //it's required here as we have the jbossws-cxf.xml descriptor)
      bus = new JBossWSSpringBusFactory().createBus((URL[])null);
      ConfiguredBeanLocator delegate = bus.getExtension(ConfiguredBeanLocator.class);
      delegatingBeanLocator = new DelegatingBeanLocator(additionalCtx, delegate);
      bus.setExtension(delegatingBeanLocator, ConfiguredBeanLocator.class);
      ctx = bus.getExtension(BusApplicationContext.class);
      //Load additional configurations from cxf-servlet.xml
      if (location != null)
      {
         try
         {
            loadAdditionalConfig(ctx, location);
         }
         catch (IOException e)
         {
            DEPLOYMENT_LOGGER.unableToLoadAdditionalConfigurationFrom(location, e);
         }
      }
      
      //Force servlet transport to prevent CXF from using Jetty / http server or other transports
      bus.setExtension(new ServletDestinationFactory(), HttpDestinationFactory.class);
   }

   /**
    * Update the Bus held by the this instance using the provided parameters.
    * This basically prepares the bus for being used with JBossWS.
    * 
    * @param resolver               The ResourceResolver to configure, if any
    * @param configurer             The JBossWSCXFConfigurer to install in the bus, if any
    * @param dep                    The current JBossWS-SPI Deployment
    * @param wsmd                   The current JBossWebservicesMetaData, if any
    * @param dep                    The current deployment
    */
   @Override
   public void configure(ResourceResolver resolver, Configurer configurer, JBossWebservicesMetaData wsmd, Deployment dep)
   {
      if (configured)
      {
         throw MESSAGES.busAlreadyConfigured(ctx);
      }
      super.configure(resolver, configurer, wsmd, dep);
      
      GenericApplicationContext jbosswsCxfContext = null;
      //load stuff from provided jbossws-cxf.xml DD
      if (jbosswsCxfLocation != null) 
      {
         try
         {
    	    jbosswsCxfContext = loadAdditionalConfig(ctx,  jbosswsCxfLocation);
         }
         catch (IOException e)
         {
            throw MESSAGES.unableToLoadConfigurationFrom(jbosswsCxfLocation, e);
         } 
      }
      
      //possibly load stuff from additional DD / DD generated from metadata if the provided jbossws-cxf.xml did not specify endpoints
      if (additionalLocations != null &&
            (jbosswsCxfLocation == null || jbosswsCxfContext.getBeansOfType(JBossWSSpringEndpointImpl.class).isEmpty()))
      {
         for (URL additionXml : additionalLocations)
         {
            try
            {
               loadAdditionalConfig(jbosswsCxfLocation != null ? jbosswsCxfContext : ctx, additionXml);
            }
            catch (IOException e)
            {
               throw MESSAGES.unableToLoadConfigurationFrom(additionXml, e);
            }
         }
      }
      configured = true;
   }

   @Override
   public Configurer createServerConfigurer(BindingCustomization customization, WSDLFilePublisher wsdlPublisher,
         List<Endpoint> depEndpoints, UnifiedVirtualFile root, String epConfigName, String epConfigFile)
   {
      ApplicationContext ctx = bus.getExtension(BusApplicationContext.class);
      ServerBeanCustomizer customizer = new ServerBeanCustomizer();
      customizer.setBindingCustomization(customization);
      customizer.setWsdlPublisher(wsdlPublisher);
      customizer.setDeploymentEndpoints(depEndpoints);
      customizer.setDeploymentRoot(root);
      customizer.setEpConfigFile(epConfigFile);
      customizer.setEpConfigName(epConfigName);
      Configurer orig = bus.getExtension(Configurer.class);
      JBossWSSpringConfigurer serverConfigurer = (orig instanceof JBossWSSpringConfigurer) ? (JBossWSSpringConfigurer)orig : new JBossWSSpringConfigurer(orig);
      serverConfigurer.setApplicationContext(ctx);
      serverConfigurer.setCustomizer(customizer);
      return serverConfigurer;
   }

   protected GenericApplicationContext loadAdditionalConfig(ApplicationContext parent , URL locationUrl)
         throws IOException
   {
      if (locationUrl == null)
         throw MESSAGES.unableToLoadAdditionalConfigFromNull();
      InputStream is = locationUrl.openStream();
      GenericApplicationContext childCtx = new GenericApplicationContext(parent);
      additionalCtx.add(childCtx);
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

   private static class DelegatingBeanLocator implements ConfiguredBeanLocator {
	   
	   private final ConfiguredBeanLocator delegate;
	   private final List<GenericApplicationContext> contexts;
	   
	   private DelegatingBeanLocator(final List<GenericApplicationContext> contexts, final ConfiguredBeanLocator delegate) {
	       this.delegate = delegate;
	       this.contexts = contexts;
	   }

	   @Override
	   public List<String> getBeanNamesOfType(Class<?> arg0) {
		   return delegate.getBeanNamesOfType(arg0);
	   }

	   // TODO: hack for WSPolicyFeature local reference resolver :(
	   @Override
	   public <T> T getBeanOfType(String arg0, Class<T> arg1) {
		   for (final GenericApplicationContext ctx : contexts) {
			   if (ctx.containsBean(arg0)) {
				   return ctx.getBean(arg0, arg1);
			   }
		   }
		   return delegate.getBeanOfType(arg0, arg1);
	   }

	   @Override
	   public <T> Collection<? extends T> getBeansOfType(Class<T> arg0) {
		   return delegate.getBeansOfType(arg0);
	   }

	   @Override
	   public boolean hasBeanOfName(String arg0) {
		   return delegate.hasBeanOfName(arg0);
	   }

	   @Override
	   public boolean hasConfiguredPropertyValue(String arg0, String arg1, String arg2) {
		   return delegate.hasConfiguredPropertyValue(arg0, arg1, arg2);
	   }

	   @Override
	   public <T> boolean loadBeansOfType(Class<T> arg0, BeanLoaderListener<T> arg1) {
		   return delegate.loadBeansOfType(arg0, arg1);
	   }
    }
}
