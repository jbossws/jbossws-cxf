package org.jboss.test.ws.jaxws.cxf.jms;

import java.util.Properties;

import javax.jms.DeliveryMode;
import javax.jms.QueueConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.cxf.transport.jms.JMSConfigFeature;
import org.apache.cxf.transport.jms.JMSConfiguration;
import org.jboss.wsf.stack.cxf.configuration.JBossWSEndpointConfigure;
import org.jboss.wsf.stack.cxf.deployment.EndpointImpl;
import org.jboss.wsf.test.JBossWSTestHelper;

public class JmsEndpintConfigure implements JBossWSEndpointConfigure
{

   @Override
   public void config(EndpointImpl endpoint)
   {
      JMSConfigFeature feature = new JMSConfigFeature();
      JMSConfiguration config = new JMSConfiguration();
      
      Properties env = new Properties();
      env.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");
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
         // TODO Auto-generated catch block
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
