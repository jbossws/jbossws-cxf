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
package org.jboss.wsf.stack.cxf.deployment.aspect;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javax.xml.ws.spi.Provider;

import org.apache.cxf.BusFactory;
import org.apache.cxf.configuration.Configurer;
import org.jboss.ws.api.binding.BindingCustomization;
import org.jboss.ws.common.integration.AbstractDeploymentAspect;
import org.jboss.ws.common.integration.WSConstants;
import org.jboss.wsf.spi.deployment.ArchiveDeployment;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.ResourceResolver;
import org.jboss.wsf.stack.cxf.client.configuration.JBossWSBusFactory;
import org.jboss.wsf.stack.cxf.client.util.DelegateClassLoader;
import org.jboss.wsf.stack.cxf.configuration.BusHolder;
import org.jboss.wsf.stack.cxf.configuration.NonSpringBusHolder;
import org.jboss.wsf.stack.cxf.configuration.SpringBusHolder;
import org.jboss.wsf.stack.cxf.deployment.WSDLFilePublisher;
import org.jboss.wsf.stack.cxf.metadata.services.DDBeans;
import org.jboss.wsf.stack.cxf.resolver.JBossWSResourceResolver;
import org.jboss.wsf.stack.cxf.transport.SoapTransportFactoryExt;

/**
 * A deployment aspect that creates the CXF Bus early and attaches it to the endpoints (wrapped in a BusHolder)
 *
 * @author alessio.soldano@jboss.com
 * @since 25-Mar-2010
 */
public class BusDeploymentAspect extends AbstractDeploymentAspect
{
   
   @SuppressWarnings("unchecked")
   protected void startDeploymentBus(Deployment dep)
   {
      ClassLoader origClassLoader = SecurityActions.getContextClassLoader();
      try
      {
         //start cleaning the BusFactory thread locals
         BusFactory.setThreadDefaultBus(null);

         ArchiveDeployment aDep = (ArchiveDeployment) dep;

         ResourceResolver deploymentResolver = aDep.getResourceResolver();
         org.apache.cxf.resource.ResourceResolver resolver = new JBossWSResourceResolver(deploymentResolver);
         Map<String, String> contextParams = (Map<String, String>) dep.getProperty(WSConstants.STACK_CONTEXT_PARAMS);
         String jbosswsCxfXml = contextParams == null ? null : contextParams.get(BusHolder.PARAM_CXF_BEANS_URL);
         BusHolder holder = null;

         //set the runtime classloader (pointing to the deployment unit) to allow CXF accessing to the classes;
         //use origClassLoader (which on AS7 is set to ASIL aggregation module's classloader by TCCLDeploymentProcessClassLoader) as
         //parent to make sure user provided libs in the deployment do no mess up the WS endpoint's deploy if they duplicates
         //libraries already available on the application server modules.
         SecurityActions.setContextClassLoader(new DelegateClassLoader(dep.getRuntimeClassLoader(), origClassLoader));
         if (jbosswsCxfXml != null) // Spring available
         {
            URL cxfServletURL = null;
            try
            {
               cxfServletURL = deploymentResolver.resolve("WEB-INF/cxf-servlet.xml");
            }
            catch (IOException e)
            {
            } //ignore, cxf-servlet.xml is optional, we might even decide not to support this

            try
            {
               holder = new SpringBusHolder(cxfServletURL, deploymentResolver.resolve(jbosswsCxfXml));
               Configurer configurer = holder.createServerConfigurer(dep.getAttachment(BindingCustomization.class),
                     new WSDLFilePublisher(aDep), dep.getService().getEndpoints(), aDep.getRootFile());
               holder.configure(new SoapTransportFactoryExt(), resolver, configurer);
            }
            catch (Exception e)
            {
               throw new RuntimeException(e); //re-throw, jboss-cxf.xml is required
            }
         }
         else
         //Spring not available
         {
            DDBeans metadata = dep.getAttachment(DDBeans.class);
            holder = new NonSpringBusHolder(metadata);
            Configurer configurer = holder.createServerConfigurer(dep.getAttachment(BindingCustomization.class),
                  new WSDLFilePublisher(aDep), dep.getService().getEndpoints(), aDep.getRootFile());
            holder.configure(new SoapTransportFactoryExt(), resolver, configurer);
         }
         dep.addAttachment(BusHolder.class, holder);
      }
      finally
      {
         //clean threadlocals in BusFactory and restore the original classloader
         BusFactory.setThreadDefaultBus(null);
         SecurityActions.setContextClassLoader(origClassLoader);
      }
   }
   
   
   
   @Override
   public void start(Deployment dep)
   {
      //Make sure the default bus is created and set for client side usage
      //(i.e. no server side integration contribution in it)
      if (BusFactory.getDefaultBus(false) == null)
      {
         JBossWSBusFactory.getDefaultBus(Provider.provider().getClass().getClassLoader());
      }
      this.startDeploymentBus(dep);
   }
   
   @Override
   public void stop(Deployment dep)
   {
      BusHolder holder = dep.removeAttachment(BusHolder.class);
      if (holder != null)
      {
         holder.close();
      }
   }
}
