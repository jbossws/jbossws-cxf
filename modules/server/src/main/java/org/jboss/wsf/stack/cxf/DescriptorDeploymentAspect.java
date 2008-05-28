/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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

//$Id$

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.logging.Logger;
import org.jboss.metadata.javaee.spec.ParamValueMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.wsf.spi.deployment.ArchiveDeployment;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.DeploymentAspect;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.Deployment.DeploymentType;
import org.jboss.wsf.spi.WSFRuntime;
import org.jboss.wsf.stack.cxf.metadata.services.DDBeans;
import org.jboss.wsf.stack.cxf.metadata.services.DDEndpoint;

/**
 * A deployer that locates or generates cxf.xml 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 10-May-2007
 */
public class DescriptorDeploymentAspect extends DeploymentAspect
{
   // provide logging
   private static final Logger log = Logger.getLogger(DescriptorDeploymentAspect.class);

   private String invokerEJB3;
   private String invokerJSE;

   public void setInvokerEJB3(String invokerEJB3)
   {
      this.invokerEJB3 = invokerEJB3;
   }

   public void setInvokerJSE(String invokerJSE)
   {
      this.invokerJSE = invokerJSE;
   }
   
   @Override
   public void create(Deployment dep, WSFRuntime runtime)
   {
      URL cxfURL = getCXFConfigFromClassLoader(dep);
      if (cxfURL == null)
      {
         cxfURL = getCXFConfigFromDeployment(dep);
         if (cxfURL == null)
         {
            cxfURL = generateCXFConfigFromDeployment(dep);
         }
         putCXFConfigToDeployment(dep, cxfURL);
      }
   }

   @Override
   public void destroy(Deployment dep, WSFRuntime runtime)
   {
      DDBeans dd = dep.getAttachment(DDBeans.class);
      if (dd != null)
      {
         dd.destroyFileURL();
      }
   }
   
   /**
    * Looks for <b>cxf.xml</b> in classloader 
    * @param dep deployment which initial classloader will be used
    * @return <b>cxf.xml URL</b> or <b>null</b> if not found
    */
   private static URL getCXFConfigFromClassLoader(Deployment dep)
   {
      ClassLoader initCL = dep.getInitialClassLoader();
      URL cxfURL = initCL.getResource("cxf.xml");
      if (cxfURL != null)
      {
         log.info("CXF configuration found: " + cxfURL);
      }
      return cxfURL;
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
   private static URL getCXFConfigFromDeployment(Deployment dep)
   {
      DeploymentType depType = dep.getType();
      
      String metadir;
      if (depType == DeploymentType.JAXWS_EJB3)
      {
         // expected resource location for EJB3 deployments
         metadir = "META-INF";
      }
      else if (depType == DeploymentType.JAXWS_JSE)
      {
         // expected resource location for POJO deployments
         metadir = "WEB-INF";
      }
      else
      {
         // only POJO and EJB3 deployments are supported
         throw new IllegalStateException("Unsupported deployment type: " + depType);
      }

      URL cxfURL = null;
      try
      {
         // get resource URL
         ArchiveDeployment archDep = (ArchiveDeployment)dep;
         cxfURL = archDep.getMetaDataFileURL(metadir + "/jbossws-cxf.xml");
         log.info("JBossWS-CXF configuration found: " + cxfURL);
      }
      catch (IOException ignore)
      {
         // resource not found
      }
      
      return cxfURL;
   }
   
   /**
    * Generated CXF descriptor from deployment
    * @param dep deployment
    * @return CXF descriptor URL
    */
   private URL generateCXFConfigFromDeployment(Deployment dep)
   {
      // Generate the jbossws-cxf.xml descriptor
      DeploymentType depType = dep.getType();
      
      DDBeans dd = new DDBeans();
      for (Endpoint ep : dep.getService().getEndpoints())
      {
         String id = ep.getShortName();
         String address = ep.getAddress();
         String implementor = ep.getTargetBeanName();

         DDEndpoint ddep = new DDEndpoint(id, address, implementor);

         if (depType == DeploymentType.JAXWS_EJB3)
         {
            ddep.setInvoker(invokerEJB3);
         }

         if (depType == DeploymentType.JAXWS_JSE)
         {
            ddep.setInvoker(invokerJSE);
         }

         log.info("Add " + ddep);
         dd.addEndpoint(ddep);
      }

      URL cxfURL = dd.createFileURL();
      log.info("JBossWS-CXF configuration generated: " + cxfURL);

      dep.addAttachment(DDBeans.class, dd);

      return cxfURL;
   }

   /**
    * Puts CXF config file to deployment property <b>org.jboss.ws.webapp.ContextParameterMap</b> map
    * @param dep deployment where to put
    * @param cxfURL to be put
    */
   private static void putCXFConfigToDeployment(Deployment dep, URL cxfURL)
   {
      // get property map
      String propKey = "org.jboss.ws.webapp.ContextParameterMap";
      Map<String, String> contextParams = (Map<String, String>)dep.getProperty(propKey);
      if (contextParams == null)
      {
         // if there's no associated map with the property create it now
         contextParams = new HashMap<String, String>();
         dep.setProperty(propKey, contextParams);
      }
      // put cxf config URL to the property map
      contextParams.put(CXFServletExt.PARAM_CXF_BEANS_URL, cxfURL.toExternalForm());
      
      boolean jbossWebMetaDataAvailable = true;
      
      try
      {
         Class.forName("org.jboss.metadata.web.jboss.JBossWebMetaData");
      }
      catch (Exception ignore)
      {
         // we are not running on AS 5
         jbossWebMetaDataAvailable = false;
      }
      
      if (jbossWebMetaDataAvailable && (dep.getType() == DeploymentType.JAXWS_EJB3))
      {
         // put cxf config URL to generated web app context params
         JBossWebMetaData jbwmd = dep.getAttachment(JBossWebMetaData.class);
         if (jbwmd != null)
         {
            boolean alreadySet = false;
            List<ParamValueMetaData> ctxParams = jbwmd.getContextParams();
            if (ctxParams == null)
            {
               ctxParams = new ArrayList<ParamValueMetaData>();
               jbwmd.setContextParams(ctxParams);
            }

            if (ctxParams.size() > 0)
            {
               for (ParamValueMetaData pvmd : ctxParams)
               {
                  if (pvmd.getParamName().equals(CXFServletExt.PARAM_CXF_BEANS_URL))
                  {
                     alreadySet = true;
                  }
               }
            }

            if (false == alreadySet)
            {
               ParamValueMetaData pvmd = new ParamValueMetaData();
               pvmd.setParamName(CXFServletExt.PARAM_CXF_BEANS_URL);
               pvmd.setParamValue(cxfURL.toExternalForm());
               ctxParams.add(pvmd);
            }
         }
      }
   }

}
