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

import org.jboss.wsf.spi.DeploymentAspectManagerLocator;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.DeploymentAspectManager;

/**
 * Endpoint servlet with WS framework aspects support called on servlet lifecycle methods
 * @author richard.opalka@jboss.com
 */
public class AspectizedEndpointServlet extends EndpointServlet
{

   protected DeploymentAspectManager aspectsManager;

   protected void initDeploymentAspectManager()
   {
      SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
      DeploymentAspectManagerLocator locator = spiProvider.getSPI(DeploymentAspectManagerLocator.class);
      aspectsManager = locator.locateDeploymentAspectManager("WSServletAspectManager");
   }
   
   protected void callRuntimeAspects()
   {
      Deployment dep = endpoint.getService().getDeployment();
      aspectsManager.create(dep, null);
      aspectsManager.start(dep, null);
   }
   
   public void destroy()
   {
      try
      {
         Deployment dep = endpoint.getService().getDeployment();
         aspectsManager.stop(dep, null);
         aspectsManager.destroy(dep, null);
      }
      finally
      {
         super.destroy();
      }
   }
   
}
