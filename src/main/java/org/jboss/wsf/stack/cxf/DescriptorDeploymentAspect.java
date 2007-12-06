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

//$Id: XFireServicesDeployer.java 3802 2007-07-05 16:44:32Z thomas.diesler@jboss.com $

import java.util.HashMap;
import java.util.Map;

import org.jboss.logging.Logger;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.DeploymentAspect;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.Deployment.DeploymentType;
import org.jboss.wsf.stack.cxf.metadata.services.DDBeans;
import org.jboss.wsf.stack.cxf.metadata.services.DDEndpoint;

/**
 * A deployer that generates cxf.xml 
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
   public void create(Deployment dep)
   {
      DeploymentType depType = dep.getType();
      if (depType != DeploymentType.JAXWS_EJB3 && depType != DeploymentType.JAXWS_JSE)
         throw new IllegalStateException("Unsupported deployment type: " + depType);

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

      dep.addAttachment(DDBeans.class, dd);

      String propKey = "org.jboss.ws.webapp.ContextParameterMap";
      Map<String, String> contextParams = (Map<String, String>)dep.getProperty(propKey);
      if (contextParams == null)
      {
         contextParams = new HashMap<String, String>();
         dep.setProperty(propKey, contextParams);
      }
      contextParams.put(CXFServletExt.PARAM_CXF_BEANS_URL, dd.createFileURL().toExternalForm());
   }

   @Override
   public void destroy(Deployment dep)
   {
      DDBeans dd = dep.getAttachment(DDBeans.class);
      if (dd != null)
      {
         dd.destroyFileURL();
      }
   }
}