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
import org.jboss.ws.common.utils.DelegateClassLoader;
import org.jboss.wsf.spi.deployment.ArchiveDeployment;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.ResourceResolver;
import org.jboss.wsf.spi.metadata.j2ee.JSEArchiveMetaData;
import org.jboss.wsf.spi.metadata.webservices.JBossWebservicesMetaData;
import org.jboss.wsf.stack.cxf.client.configuration.JBossWSBusFactory;
import org.jboss.wsf.stack.cxf.configuration.BusHolder;
import org.jboss.wsf.stack.cxf.configuration.NonSpringBusHolder;
import org.jboss.wsf.stack.cxf.configuration.SpringBusHolder;
import org.jboss.wsf.stack.cxf.deployment.WSDLFilePublisher;
import org.jboss.wsf.stack.cxf.metadata.services.DDBeans;
import org.jboss.wsf.stack.cxf.resolver.JBossWSResourceResolver;

/**
 * A deployment aspect that creates the CXF Bus early and attaches it to the endpoints (wrapped in a BusHolder)
 *
 * @author alessio.soldano@jboss.com
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class BusDeploymentAspect extends AbstractDeploymentAspect
{
   
   @Override
   public void start(final Deployment dep)
   {
      if (BusFactory.getDefaultBus(false) == null)
      {
         //Make sure the default bus is created and set for client side usage
         //(i.e. no server side integration contribution in it)
         JBossWSBusFactory.getDefaultBus(Provider.provider().getClass().getClassLoader());
      }
      startDeploymentBus(dep);
   }
   
   @Override
   public void stop(final Deployment dep)
   {
      final BusHolder holder = dep.removeAttachment(BusHolder.class);
      if (holder != null)
      {
         holder.close();
      }
   }

   @SuppressWarnings("unchecked")
   private void startDeploymentBus(final Deployment dep)
   {
      BusFactory.setThreadDefaultBus(null);
      ClassLoader origClassLoader = SecurityActions.getContextClassLoader();
      try
      {
         final ArchiveDeployment aDep = (ArchiveDeployment) dep;
         final ResourceResolver deploymentResolver = aDep.getResourceResolver();
         final org.apache.cxf.resource.ResourceResolver resolver = new JBossWSResourceResolver(deploymentResolver);
         Map<String, String> contextParams = (Map<String, String>) dep.getProperty(WSConstants.STACK_CONTEXT_PARAMS);
         String jbosswsCxfXml = contextParams == null ? null : contextParams.get(BusHolder.PARAM_CXF_BEANS_URL);
         BusHolder holder = null;

         //set the runtime classloader (pointing to the deployment unit) to allow CXF accessing to the classes;
         //use origClassLoader (which on AS7 is set to ASIL aggregation module's classloader by TCCLDeploymentProcessClassLoader) as
         //parent to make sure user provided libs in the deployment do no mess up the WS endpoint's deploy if they duplicates
         //libraries already available on the application server modules.
         SecurityActions.setContextClassLoader(new DelegateClassLoader(dep.getRuntimeClassLoader(), origClassLoader));
         if (jbosswsCxfXml != null)
         {
            // Spring available and jbossws-cxf.xml provided
            final URL cxfServletUrl = getResourceUrl(deploymentResolver, "WEB-INF/cxf-servlet.xml", false); // TODO: decide not to support this?
            final URL jbosswsCxfUrl = getResourceUrl(deploymentResolver, jbosswsCxfXml, true);
            holder = new SpringBusHolder(cxfServletUrl, jbosswsCxfUrl);
         }
         else
         {
            // Spring not available or jbossws-cxf.xml not provided
            DDBeans metadata = dep.getAttachment(DDBeans.class);
            holder = new NonSpringBusHolder(metadata);
         }
         
         String epConfigName = null;
         String epConfigFile = null;
         JSEArchiveMetaData jsemd = dep.getAttachment(JSEArchiveMetaData.class);
         JBossWebservicesMetaData wsmd = dep.getAttachment(JBossWebservicesMetaData.class);
         //first check JSEArchiveMetaData as that has the actual merged value for POJO deployments
         if (jsemd != null) {
            epConfigName = jsemd.getConfigName();
            epConfigFile = jsemd.getConfigFile();
         } else if (wsmd != null) {
            epConfigName = wsmd.getConfigName();
            epConfigFile = wsmd.getConfigFile();
         }
         
         Configurer configurer = holder.createServerConfigurer(dep.getAttachment(BindingCustomization.class),
               new WSDLFilePublisher(aDep), dep.getService().getEndpoints(), aDep.getRootFile(), epConfigName, epConfigFile);
         holder.configure(resolver, configurer, wsmd);
         dep.addAttachment(BusHolder.class, holder);
      }
      finally
      {
         BusFactory.setThreadDefaultBus(null);
         SecurityActions.setContextClassLoader(origClassLoader);
      }
   }

   private static URL getResourceUrl(final ResourceResolver resolver, final String resourcePath, final boolean fail) {
       try {
           return resolver.resolve(resourcePath);
       } catch (final IOException e) {
           if (fail) {
               throw new RuntimeException(e);
           } else {
               return null;
           }
       }
   }
   
}
