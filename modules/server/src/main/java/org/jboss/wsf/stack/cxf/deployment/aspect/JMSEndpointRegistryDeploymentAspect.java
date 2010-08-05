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

import java.lang.reflect.Method;
import java.util.Map;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.endpoint.ServerRegistry;
import org.apache.cxf.frontend.MethodDispatcher;
import org.apache.cxf.frontend.SimpleMethodDispatcher;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.jms.JMSConfiguration;
import org.apache.cxf.transport.jms.JMSDestination;
import org.jboss.wsf.common.integration.JMSDeploymentAspect;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.JMSEndpoint;
import org.jboss.wsf.spi.management.EndpointRegistry;
import org.jboss.wsf.spi.management.EndpointRegistryFactory;
import org.jboss.wsf.stack.cxf.client.util.SpringUtils;

/**
 * The DeploymentAspect to register the jms endpoints
 * 
 * @author <a href="ema@redhat.com">Jim Ma</a>
 */
public class JMSEndpointRegistryDeploymentAspect extends JMSDeploymentAspect
{
   private JMSEndpointRegistryDeploymentAspectDelegate aspect;
   
   public JMSEndpointRegistryDeploymentAspect() 
   {
      if (SpringUtils.SPRING_AVAILABLE) 
      {
         aspect = new JMSEndpointRegistryDeploymentAspectDelegate();
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
