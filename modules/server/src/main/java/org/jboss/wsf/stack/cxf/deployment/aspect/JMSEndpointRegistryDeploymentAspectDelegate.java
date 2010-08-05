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

/**
 * The DeploymentAspect to register the jms endpoints
 * 
 * @author <a href="ema@redhat.com">Jim Ma</a>
 */
class JMSEndpointRegistryDeploymentAspectDelegate extends JMSDeploymentAspect
{
   private EndpointRegistry registry = null;
   @Override
   public void start(Deployment dep)  
   {
      SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
      if (registry == null)
         registry = spiProvider.getSPI(EndpointRegistryFactory.class).getEndpointRegistry();
      Bus bus = dep.getAttachment(Bus.class);
      Map<String, JMSConfiguration> jmsConfigMap = createEndpointJmsConfigMap(bus);
      for (Endpoint endpoint : dep.getService().getEndpoints()) 
      {
         JMSEndpoint jmsEndpoint = (JMSEndpoint)endpoint;
         String endpointImplClass = jmsEndpoint.getTargetBeanName();
         JMSConfiguration jmsConfig = jmsConfigMap.get(endpointImplClass);
         if (jmsConfig != null) 
         {  
            jmsEndpoint.setTargetDestination(jmsConfig.getTargetDestination());
            jmsEndpoint.setReplyDestination(jmsConfig.getReplyDestination());            
         }
         
         registry.register(jmsEndpoint);
      }
   }
   
   public void stop(Deployment dep)
   {
      SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
      if (registry == null)
         registry = spiProvider.getSPI(EndpointRegistryFactory.class).getEndpointRegistry();
      for (Endpoint ep : dep.getService().getEndpoints())
      {
         registry.unregister(ep);
      }
   }
      
   private Map<String, JMSConfiguration> createEndpointJmsConfigMap(Bus bus) 
   {
      Map<String, JMSConfiguration> endpointJmsConfigMap = new java.util.HashMap<String, JMSConfiguration>();
      ServerRegistry serverRegsitry = bus.getExtension(ServerRegistry.class);
      for (Server server : serverRegsitry.getServers()) 
      {
         Destination destination = server.getDestination();
         if (destination instanceof JMSDestination) 
         {
            JMSConfiguration jmsConfiguration = ((JMSDestination)destination).getJmsConfig();
            String implClassName = getEndpointClassName(server);
            if (implClassName != null) 
            {
               endpointJmsConfigMap.put(implClassName, jmsConfiguration);
            }
         }        
      }
      return endpointJmsConfigMap;
   }
     
   private String getEndpointClassName(Server server)
   {
      MethodDispatcher methodDispatcher = (SimpleMethodDispatcher) server.getEndpoint().getService().get(
            MethodDispatcher.class.getName());
      if (methodDispatcher != null && methodDispatcher instanceof SimpleMethodDispatcher)
      {
         Method method = ((SimpleMethodDispatcher)methodDispatcher).getPrimaryMethod(server.getEndpoint().getEndpointInfo().getInterface()
               .getOperations().iterator().next());
         return method != null ? method.getDeclaringClass().getName() : null;
      }
      return null;
   }
}
