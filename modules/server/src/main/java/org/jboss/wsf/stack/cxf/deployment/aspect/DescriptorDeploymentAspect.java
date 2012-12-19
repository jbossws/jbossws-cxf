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

import static org.jboss.wsf.spi.deployment.DeploymentType.JAXRPC;
import static org.jboss.ws.common.integration.WSHelper.isJaxrpcDeployment;
import static org.jboss.ws.common.integration.WSHelper.isJseDeployment;
import static org.jboss.ws.common.integration.WSHelper.isWarArchive;
import static org.jboss.wsf.stack.cxf.Loggers.DEPLOYMENT_LOGGER;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.jboss.ws.common.integration.AbstractDeploymentAspect;
import org.jboss.ws.common.integration.WSConstants;
import org.jboss.wsf.spi.deployment.ArchiveDeployment;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.stack.cxf.Messages;
import org.jboss.wsf.stack.cxf.client.Constants;
import org.jboss.wsf.stack.cxf.client.util.SpringUtils;
import org.jboss.wsf.stack.cxf.configuration.BusHolder;
import org.jboss.wsf.stack.cxf.metadata.MetadataBuilder;
import org.jboss.wsf.stack.cxf.metadata.services.DDBeans;

/**
 * A deployer that locates or generates cxf.xml 
 *
 * @author Thomas.Diesler@jboss.org
 * @author alessio.soldano@jboss.com
 * @since 10-May-2007
 */
public class DescriptorDeploymentAspect extends AbstractDeploymentAspect
{
   private static final boolean PREFER_SPRING_DESCRIPTOR_GENERATION = Boolean.getBoolean("org.jboss.ws.cxf.prefer_spring_descriptor_generation");
   
   @Override
   public void start(Deployment dep)
   {
      URL cxfURL = null;
      if (SpringUtils.isSpringAvailable())
      {
         //only try reading jbossws-cxf.xml if Spring available...
         cxfURL = getCXFConfigFromDeployment(dep);
         //... but do not generate it if it's not provided
         //or unless it's explicitly required to be generated
         if (cxfURL == null && PREFER_SPRING_DESCRIPTOR_GENERATION)
         {
            DDBeans dd = generateMetadataFromDeployment(dep);
            cxfURL = dd.createFileURL();
            DEPLOYMENT_LOGGER.jbwscxfConfGenerated(cxfURL);
         }
      }
      if (cxfURL == null)
      {
         generateMetadataFromDeployment(dep);
      }
      else
      {
         DEPLOYMENT_LOGGER.actualConfFromFile(cxfURL);
         putCXFConfigToDeployment(dep, cxfURL);
      }
   }

   @Override
   public void stop(Deployment dep)
   {
      DDBeans dd = dep.getAttachment(DDBeans.class);
      if (dd != null)
      {
         dd.destroyFileURL();
      }
   }
  
   /**
    * Looks for <b>jbossws-cxf.xml</b> in:
    * <ul>
    *   <li><b>META-INF</b> resource directory for EJB3 deployment</li>
    *   <li><b>WEB-INF</b> resource directory for POJO deployment</li>
    * </ul>
    * @param dep deployment where to look for resources
    * @return <b>jbossws-cxf.xml URL</b> or <b>null</b> if not found
    */
   private URL getCXFConfigFromDeployment(Deployment dep)
   {
      if (isJaxrpcDeployment(dep))
      {
         throw Messages.MESSAGES.unsupportedDeploymentType(JAXRPC);
      }
      String metadir = null;
      if (isJseDeployment(dep) || isWarArchive(dep))
      {
         // expected resource location for POJO deployments
         // if EJBs are bundled in WARs, we default to WEB-INF
         metadir = "WEB-INF";
      }
      else
      {
         // expected resource location for EJB3 deployments
         metadir = "META-INF";
      }

      URL cxfURL = null;
      //get resource URL
      ArchiveDeployment archDep = (ArchiveDeployment)dep;
      cxfURL = archDep.getResourceResolver().resolveFailSafe(metadir + "/" + Constants.JBOSSWS_CXF_SPRING_DD);
      if (cxfURL != null)
         DEPLOYMENT_LOGGER.jbwscxfConfFound(cxfURL);
      
      return cxfURL;
   }
   
   /**
    * Generates the jbossws-cxf metadata from the deployment
    * and attaches it the deployment
    * @param dep
    * @return
    */
   private DDBeans generateMetadataFromDeployment(Deployment dep)
   {
      MetadataBuilder builder = new MetadataBuilder();
      DDBeans dd = builder.build(dep);
      dep.addAttachment(DDBeans.class, dd);
      return dd;
   }

   /**
    * Puts CXF config file reference to the stack specific context properties. 
    *
    * @param dep webservice deployment
    * @param cxfURL CXF DD URL
    * @see org.jboss.ws.common.integration.WSConstants.STACK_CONTEXT_PARAMS
    */
   @SuppressWarnings("unchecked")
   private void putCXFConfigToDeployment(Deployment dep, URL cxfURL)
   {
      // get property map
      Map<String, String> contextParams = (Map<String, String>)dep.getProperty(WSConstants.STACK_CONTEXT_PARAMS);
      if (contextParams == null)
      {
         // if there's no associated map with the property create it now
         contextParams = new HashMap<String, String>();
         dep.setProperty(WSConstants.STACK_CONTEXT_PARAMS, contextParams);
      }
      // put cxf config URL to the property map
      contextParams.put(BusHolder.PARAM_CXF_BEANS_URL, cxfURL.toExternalForm());
   }

}
