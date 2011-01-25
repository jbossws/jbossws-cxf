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

import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.stack.cxf.config.CXFInitializer;

/**
 * A synchronized version of the bus deployment aspect that waits for the CXFInitializer
 * and does single-thread deployment processing
 *
 * @author alessio.soldano@jboss.com
 * @since 25-Mar-2010
 */
public class SynchronizedBusDeploymentAspect extends BusDeploymentAspect
{
   @Override
   protected void startDeploymentBus(Deployment dep)
   {
      //ensure the default bus has been set on the server, then proceed
      CXFInitializer.waitForDefaultBusAvailability();
      //synchronize as this assumes nothing deals with the BusFactory threadlocals associated with the system daemon
      //thread doing the deployments, iow multiple concurrent deployment are not supported in this deployment aspect
      synchronized (this)
      {
         super.startDeploymentBus(dep);
      }
   }
   
   @Override
   public void start(Deployment dep)
   {
      this.startDeploymentBus(dep);
   }
}