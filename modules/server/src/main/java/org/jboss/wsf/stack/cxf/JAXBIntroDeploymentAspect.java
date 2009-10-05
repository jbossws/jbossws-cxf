/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf;

import java.io.IOException;
import java.io.InputStream;

import org.jboss.jaxb.intros.IntroductionsAnnotationReader;
import org.jboss.jaxb.intros.IntroductionsConfigParser;
import org.jboss.jaxb.intros.configmodel.JaxbIntros;
import org.jboss.logging.Logger;
import org.jboss.wsf.spi.binding.BindingCustomization;
import org.jboss.wsf.spi.deployment.ArchiveDeployment;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.DeploymentAspect;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.UnifiedVirtualFile;

import com.sun.xml.bind.api.JAXBRIContext;

/**
 * A CXF version of JAXBIntroDeploymentAspect, which installs JAXB Introduction 
 * metadata into the deployment for later consumption. Despite being the same as the
 * Native one, this deployment aspect is not moved to jbossws-framework as it directly
 * depends on jaxb impl and we don't really want to enforce the same jaxb impl version
 * dependency on different stacks).
 * 
 * @author Heiko.Braun@jboss.com
 * @author alessio.soldano@jboss.com
 */
public class JAXBIntroDeploymentAspect extends DeploymentAspect
{
   private static Logger logger = Logger.getLogger(JAXBIntroDeploymentAspect.class);
   private static final String META_INF_JAXB_INTROS_XML = "META-INF/jaxb-intros.xml";
   private static final String WEB_INF_JAXB_INTROS_XML = "WEB-INF/jaxb-intros.xml";

   public void start(Deployment deployment)
   {
      // assert ArchiveDeployment
      if(! (deployment instanceof ArchiveDeployment) )
      {
         if (log.isDebugEnabled())
            log.debug("JAXBIntroDeploymentAspect doesn't work on " + deployment.getClass());
         return;
      }

      ArchiveDeployment archive = (ArchiveDeployment)deployment;
      InputStream introsConfigStream = null;

      try
      {
         // META-INF first
         UnifiedVirtualFile vfs = archive.getRootFile().findChild(META_INF_JAXB_INTROS_XML);
         introsConfigStream = vfs.toURL().openStream();
      } catch (Exception e) {}

      if(null == introsConfigStream)
      {
         try 
         {
            // WEB-INF second
            UnifiedVirtualFile vfs = archive.getRootFile().findChild(WEB_INF_JAXB_INTROS_XML);
            introsConfigStream = vfs.toURL().openStream();
         } catch (Exception e) {
            return;
         }
      }
      
      try
      {

         if(introsConfigStream != null)
         {
            JaxbIntros jaxbIntros = IntroductionsConfigParser.parseConfig(introsConfigStream);
            IntroductionsAnnotationReader annotationReader = new IntroductionsAnnotationReader(jaxbIntros);
            String defaultNamespace = jaxbIntros.getDefaultNamespace();
            BindingCustomization jaxbCustomizations = new org.jboss.wsf.stack.cxf.binding.JAXBBindingCustomization();

            jaxbCustomizations.put(JAXBRIContext.ANNOTATION_READER, annotationReader);
            if(defaultNamespace != null) {
               jaxbCustomizations.put(JAXBRIContext.DEFAULT_NAMESPACE_REMAP, defaultNamespace);
            }

            // JBossWSBeanConfigurer#configureService becomes the consumer later on
            for(Endpoint endpoint : deployment.getService().getEndpoints())
            {
               endpoint.addAttachment(BindingCustomization.class, jaxbCustomizations);
            }

         }

      }
      finally
      {
         if(introsConfigStream != null)
         {
            try {
               introsConfigStream.close();
            } catch (IOException e) {
               logger.error("[" + deployment.getService().getContextRoot() + "] Error closing JAXB Introductions Configurations stream ", e);
            }
         }
      }
   }
}
