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
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.endpoint.ServerRegistry;
import org.apache.cxf.transport.jms.JMSConfiguration;
import org.apache.cxf.transport.jms.JMSDestination;
import org.jboss.wsf.common.integration.JMSDeploymentAspect;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.metadata.jms.JMSEndpointsMetaData;
import org.jboss.wsf.stack.cxf.client.util.SpringUtils;
import org.jboss.wsf.stack.cxf.configuration.BusHolder;
import org.springframework.jms.connection.SingleConnectionFactory;

/**
 * To start the jms endpoints
 * 
 * @author <a href="ema@redhat.com">Jim Ma</a>
 */
public class JMSEndpointDeploymentAspect extends JMSDeploymentAspect
{  
   private JMSEndpointDeploymentAspectDelegate aspect;
   
   public JMSEndpointDeploymentAspect() 
   {
      if (SpringUtils.SPRING_AVAILABLE) 
      {
         aspect = new JMSEndpointDeploymentAspectDelegate();
      }
   }
   
   
   @Override
   public void start(Deployment dep)  
   {
      if (aspect != null)
      {
         aspect.start(dep);
      }
   }
   
   public void stop(Deployment dep)
   {
      if (aspect != null)
      {
         aspect.stop(dep);
      }
   }
}

