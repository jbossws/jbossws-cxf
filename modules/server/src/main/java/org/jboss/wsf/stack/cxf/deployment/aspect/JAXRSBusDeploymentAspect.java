/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat Middleware LLC, and individual contributors
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

import javax.xml.ws.spi.Provider;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.transport.http.HttpDestinationFactory;
import org.apache.cxf.transport.servlet.ServletDestinationFactory;
import org.jboss.ws.common.integration.AbstractDeploymentAspect;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.stack.cxf.client.configuration.JBossWSBusFactory;

/**
 * A deployment aspect that creates the CXF Bus early and attaches it to the deployment
 *
 * @author alessio.soldano@jboss.com
 */
//TODO!!! unify with JAXWS handling
public class JAXRSBusDeploymentAspect extends AbstractDeploymentAspect
{

   @Override
   public void start(final Deployment dep)
   {
      if (BusFactory.getDefaultBus(false) == null)
      {
         //Make sure the default bus is created and set for client side usage
         //(i.e. no server side integration contribution in it)
         //TODO!!! think about this... is it still fine for the default bus to be created like this?
         JBossWSBusFactory.getDefaultBus(Provider.provider().getClass().getClassLoader());
      }
      startDeploymentBus(dep);
   }

   @Override
   public void stop(final Deployment dep)
   {
      final Bus bus = dep.removeAttachment(Bus.class);
      if (bus != null)
      {
         bus.shutdown(true);
      }
   }

   private void startDeploymentBus(final Deployment dep)
   {
      BusFactory.setThreadDefaultBus(null);
      ClassLoader origClassLoader = SecurityActions.getContextClassLoader();
      try
      {
         //at least for now, we use the classloader-bus association as a shortcut for bus retieval in the servlet...
         Bus bus = JBossWSBusFactory.getClassLoaderDefaultBus(dep.getClassLoader());
         //Force servlet transport to prevent CXF from using Jetty / http server or other transports
         bus.setExtension(new ServletDestinationFactory(), HttpDestinationFactory.class);
         //let's leave the resources and such creations in the CXF servlet for now; we'll later move that here and use a different servlet
         dep.addAttachment(Bus.class, bus);
      }
      finally
      {
         BusFactory.setThreadDefaultBus(null);
         SecurityActions.setContextClassLoader(origClassLoader);
      }
   }

}
