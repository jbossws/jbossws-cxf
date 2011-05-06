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

import java.net.URL;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.jboss.ws.common.integration.JMSDeploymentAspect;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.metadata.jms.JMSEndpointsMetaData;
import org.jboss.wsf.stack.cxf.client.configuration.JBossWSSpringBusFactory;
import org.jboss.wsf.stack.cxf.config.CXFInitializer;

/**
 * To start the jms endpoints
 * 
 * @author <a href="ema@redhat.com">Jim Ma</a>
 */
public class JMSEndpointDeploymentAspectDelegate extends JMSDeploymentAspect
{      
   @Override
   public void start(Deployment dep)  
   {
      //ensure the default bus has been set on the server, then proceed
      CXFInitializer.waitForDefaultBusAvailability();
      //TODO:handler JAXBIntro  
      if (dep.getAttachment(JMSEndpointsMetaData.class) != null)
      {
         JMSEndpointsMetaData jmsEndpoints = dep.getAttachment(JMSEndpointsMetaData.class);
         URL url = jmsEndpoints.getDescriptorURL();
         
         ClassLoader origClassLoader = SecurityActions.getContextClassLoader();
         try
         {
            SecurityActions.setContextClassLoader(dep.getRuntimeClassLoader());
            Bus bus = new JBossWSSpringBusFactory().createBus(url);
            dep.addAttachment(Bus.class, bus);
         }
         catch (Exception e)
         {
            log.error("Failed to deploy jms endpoints deployment " + url);
            throw new RuntimeException(e);
         }

         finally
         {
            BusFactory.setThreadDefaultBus(null);
            SecurityActions.setContextClassLoader(origClassLoader);
         }
      }
   }

   @Override
   public void stop(Deployment dep)
   {
      log.debugf("Undeploying jms endpoints in %s", dep.getSimpleName());
      if (dep.getAttachment(Bus.class) != null)
      {
         Bus bus = dep.getAttachment(Bus.class);
         bus.shutdown(false);
      }
   }
}

