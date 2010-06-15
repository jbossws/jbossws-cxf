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
package org.jboss.wsf.stack.cxf.deployment.aspect;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.apache.cxf.BusFactory;
import org.apache.cxf.configuration.Configurer;
import org.jboss.wsf.common.integration.AbstractDeploymentAspect;
import org.jboss.wsf.common.integration.WSConstants;
import org.jboss.wsf.spi.binding.BindingCustomization;
import org.jboss.wsf.spi.deployment.ArchiveDeployment;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.ResourceResolver;
import org.jboss.wsf.stack.cxf.configuration.BusHolder;
import org.jboss.wsf.stack.cxf.deployment.WSDLFilePublisher;
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
   @Override
   public void start(Deployment dep)
   {
      BusHolder holder;
      ClassLoader origClassLoader = SecurityActions.getContextClassLoader();
      try
      {
         ArchiveDeployment aDep = (ArchiveDeployment)dep;
         //set the runtime classloader (pointing to the deployment unit) to allow CXF accessing to the classes
         SecurityActions.setContextClassLoader(dep.getRuntimeClassLoader());
         
         ResourceResolver deploymentResolver = aDep.getResourceResolver();

         URL cxfServletURL = null;
         try
         {
            cxfServletURL = deploymentResolver.resolve("WEB-INF/cxf-servlet.xml");
         }
         catch (IOException e)
         {
         } //ignore, cxf-servlet.xml is optional, we might even decide not to support this

         holder = BusHolder.create(cxfServletURL);

         Map<String, String> contextParams = (Map<String, String>)dep.getProperty(WSConstants.STACK_CONTEXT_PARAMS);
         try
         {
            URL jbossCxfXml = deploymentResolver.resolve(contextParams.get(BusHolder.PARAM_CXF_BEANS_URL));
            org.apache.cxf.resource.ResourceResolver resolver = new JBossWSResourceResolver(deploymentResolver);
            Configurer configurer = holder.createServerConfigurer(dep.getAttachment(BindingCustomization.class), new WSDLFilePublisher(aDep));
            holder.configure(jbossCxfXml, new SoapTransportFactoryExt(), resolver, configurer);
         }
         catch (IOException e)
         {
            throw new RuntimeException(e); //re-throw, jboss-cxf.xml is required
         }
      }
      finally
      {
         //clean threadlocals in BusFactory and restore the original classloader
         BusFactory.setDefaultBus(null);
         BusFactory.setThreadDefaultBus(null);
         SecurityActions.setContextClassLoader(origClassLoader);
      }
      
      dep.addAttachment(BusHolder.class, holder);
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
