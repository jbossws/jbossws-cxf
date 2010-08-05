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
import org.jboss.wsf.stack.cxf.configuration.BusHolder;
import org.springframework.jms.connection.SingleConnectionFactory;

/**
 * To start the jms endpoints
 * 
 * @author <a href="ema@redhat.com">Jim Ma</a>
 */
public class JMSEndpointDeploymentAspectDelegate extends JMSDeploymentAspect
{  
   private BusHolder busHolder = null;   
   
   @Override
   public void start(Deployment dep)  
   {
      //TODO:handler JAXBIntro  
      if (dep.getAttachment(JMSEndpointsMetaData.class) != null)
      {
         JMSEndpointsMetaData jmsEndpoints = dep.getAttachment(JMSEndpointsMetaData.class);
         URL url = jmsEndpoints.getDescriptorURL();
         
         ClassLoader origClassLoader = SecurityActions.getContextClassLoader();
         try
         {
            SecurityActions.setContextClassLoader(dep.getRuntimeClassLoader());
            SpringBusFactory bf = new SpringBusFactory();
            Bus bus = bf.createBus(url);
            dep.addAttachment(Bus.class, bus);
         }
         catch (Exception e)
         {
            log.error("Failed to deploy jms endpoints deployment " + url);
            throw new RuntimeException(e);
         }

         finally
         {
            BusFactory.setDefaultBus(null);
            SecurityActions.setContextClassLoader(origClassLoader);
         }
      }
   }

   @Override
   public void stop(Deployment dep)
   {
      log.debug("Undeploying jms endpoints in " + dep.getSimpleName());
      if (busHolder != null && busHolder.getBus() != null)
      {
         //CXF uses WrappedConnectionFactory to create "jmsListener". DefaultMessageListenerContainer.shutdown() can not colse all the jms connections.  
         //We need to explicitly call detroy() to close connection. This should be fixed in CXF side.
         SingleConnectionFactory connectionFactory = null;
         Server jmsServer = null;
         ServerRegistry serRegistry = busHolder.getBus().getExtension(ServerRegistry.class);
         for (Server server : serRegistry.getServers())
         {
            if (server.getDestination() != null && server.getDestination() instanceof JMSDestination)
            {
               JMSDestination jmsDestination = (JMSDestination) server.getDestination();
               JMSConfiguration jmsConfig = jmsDestination.getJmsConfig();
               if (jmsConfig.getWrappedConnectionFactory() != null
                     && jmsConfig.getWrappedConnectionFactory() instanceof SingleConnectionFactory)
               {
                  connectionFactory = (SingleConnectionFactory) jmsConfig
                        .getWrappedConnectionFactory();
                  jmsServer = server;
               }

            }
            
         }
         
         if (jmsServer != null) 
         {
             jmsServer.stop();   
         }
               
         if (connectionFactory != null) 
         {
            connectionFactory.destroy();
         }
         //TODO:Remove above code after fix CXF-2788
         //close LifecycleListener if exists
         busHolder.getBus().shutdown(false);
         busHolder.close();
      }
   }
}

