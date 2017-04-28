/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.jms;

import java.util.Properties;

import javax.jms.DeliveryMode;
import javax.jms.QueueConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.cxf.transport.jms.JMSConfigFeature;
import org.apache.cxf.transport.jms.JMSConfiguration;
import org.jboss.wsf.stack.cxf.configuration.JBossWSEndpointConfig;
import org.jboss.wsf.stack.cxf.deployment.EndpointImpl;
/**
 * To config the endpoint with JMSConfigFeature
 *
 * @author ema@redhat.com
 */
public class JmsEndpintConfig implements JBossWSEndpointConfig
{

   @Override
   public void config(EndpointImpl endpoint)
   {
      JMSConfigFeature feature = new JMSConfigFeature();
      JMSConfiguration config = new JMSConfiguration();
      
      Properties env = new Properties();
      env.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");
      //TODO: look at get hostname and port from a service
      env.put(Context.PROVIDER_URL, "remote+http://localhost:43080");
      env.put(Context.SECURITY_PRINCIPAL, "kermit");
      env.put(Context.SECURITY_CREDENTIALS, "thefrog");
      InitialContext context = null;
      try {
         context = new InitialContext(env);
      } catch (Exception e) {
         e.printStackTrace();
      }
      QueueConnectionFactory connectionFactory = null;
      try
      {
         connectionFactory = (QueueConnectionFactory)context.lookup("jms/RemoteConnectionFactory");
      }
      catch (NamingException e)
      {
         e.printStackTrace();
      }
      
      config.setSessionTransacted(true);
      config.setConnectionFactory(connectionFactory);
      config.setUserName("kermit");
      config.setPassword("thefrog");
      config.setRequestURI("testQueue");
      config.setTargetDestination("testQueue");

      config.setReplyDestination("testQueue");
      config.setReplyToDestination("testQueue");
      config.setReceiveTimeout(2000L);
      config.setDeliveryMode(DeliveryMode.PERSISTENT);
      feature.setJmsConfig(config);
      endpoint.getFeatures().add(feature);
   }

}
