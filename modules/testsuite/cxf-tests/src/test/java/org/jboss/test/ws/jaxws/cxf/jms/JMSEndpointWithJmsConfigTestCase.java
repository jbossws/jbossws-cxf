/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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

import java.io.File;
import java.net.URL;
import java.util.Properties;

import javax.jms.DeliveryMode;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.jms.JMSConduit;
import org.apache.cxf.transport.jms.JMSConfigFeature;
import org.apache.cxf.transport.jms.JMSConfiguration;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test case for deploying an archive with a JMS (SOAP-over-JMS 1.0) endpoint and config with JMSConfigFeature
 * Add these attribute to make the deliver DLQ quickly: max-delivery-attempts="1" redelivery-delay="500" 
 * <address-setting name="#" dead-letter-address="jms.queue.DLQ" expiry-address="jms.queue.ExpiryQueue" max-delivery-attempts="1" redelivery-delay="500" max-size-bytes="10485760" page-size-bytes="2097152" message-counter-history-day-limit="10"/>
 * @author ema@redhat.com
 */
@RunWith(Arquillian.class)
public class JMSEndpointWithJmsConfigTestCase extends JBossWSTest
{
   private static final String JMS_SERVER = "jms";

   private static boolean useHornetQ() {
      return JBossWSTestHelper.isTargetWildFly9();
   }


   @Deployment(name="jaxws-cxf-jmsconfig-deployment",testable = false)
   @TargetsContainer(JMS_SERVER)
   public static JavaArchive createJarDeployment() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class,"jaxws-cxf-jms-only-deployment.jar");
         archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: " + (useHornetQ() ? "org.hornetq" : "org.apache.activemq.artemis" + ",org.jboss.ws.cxf.jbossws-cxf-server,org.apache.cxf.impl\n")))
                .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jms/META-INF/services/org.jboss.wsf.stack.cxf.configuration.JBossWSEndpointConfig"), "services/org.jboss.wsf.stack.cxf.configuration.JBossWSEndpointConfig")
               .addClass(org.jboss.test.ws.jaxws.cxf.jms.HelloWorld.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.jms.HelloWorldImpl.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.jms.JmsEndpintConfig.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jms/META-INF/wsdl/HelloWorldService.wsdl"), "wsdl/HelloWorldService.wsdl");
      //archive.as(ZipExporter.class).exportTo(new File("someplace/somename.jar"), true);
      return archive;
   }

   @Test
   @RunAsClient
   
   //the test for setting jms configuration like sessionTransacted, transactionManager
   public void testJMSEndpointWithJmsConfigFeature() throws Exception
   {
      if (JBossWSTestHelper.isTargetWildFly10() || JBossWSTestHelper.isTargetWildFly9()) {
         System.out.println("This test is for new feature in wildfly 11");
         return;
      }
      URL wsdlUrl = getResourceURL("jaxws/cxf/jms/META-INF/wsdl/HelloWorldService.wsdl");
      QName serviceName = new QName("http://org.jboss.ws/jaxws/cxf/jms", "HelloWorldService");

      JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
      factory.setWsdlLocation(wsdlUrl.toString());
      factory.setServiceName(serviceName);
      factory.setEndpointName(new QName("http://org.jboss.ws/jaxws/cxf/jms", "HelloWorldImplPort"));
      factory.setAddress("jms://");
      JMSConfigFeature feature = new JMSConfigFeature();
      JMSConfiguration config = new JMSConfiguration();
      
      Properties env = new Properties();
      env.put(Context.INITIAL_CONTEXT_FACTORY, getInitialContextFactory());
      env.put(Context.PROVIDER_URL, getRemotingProtocol() + "://" + getServerHost() + ":" + getServerPort(CXF_TESTS_GROUP_QUALIFIER, JMS_SERVER));
      env.put(Context.SECURITY_PRINCIPAL, JBossWSTestHelper.getTestUsername());
      env.put(Context.SECURITY_CREDENTIALS, JBossWSTestHelper.getTestPassword());
      InitialContext context = null;
      try {
         context = new InitialContext(env);
      } catch (Exception e) {
         rethrowAndHandleAuthWarning(e);
      }
      QueueConnectionFactory connectionFactory = (QueueConnectionFactory)context.lookup("jms/RemoteConnectionFactory");
      
      config.setSessionTransacted(true);
      config.setConnectionFactory(connectionFactory);
      config.setUserName(JBossWSTestHelper.getTestUsername());
      config.setPassword(JBossWSTestHelper.getTestPassword());
      config.setRequestURI("testQueue");
      config.setTargetDestination("testQueue");

      config.setReplyDestination("testQueue");
      config.setReplyToDestination("testQueue");
      config.setReceiveTimeout(1000L);
      config.setDeliveryMode(DeliveryMode.PERSISTENT);
      
      feature.setJmsConfig(config);
      factory.getFeatures().add(feature);
      HelloWorld greeter = factory.create(HelloWorld.class);

      try {
          greeter.greetMe("exception");
      }catch (javax.xml.ws.soap.SOAPFaultException e) {
         e.printStackTrace();
      }
      finally {
          ((java.io.Closeable)greeter).close();
      }

      QueueConnection con = connectionFactory.createQueueConnection(JBossWSTestHelper.getTestUsername(), JBossWSTestHelper.getTestPassword());
      QueueSession session = con.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
      Queue dlq = session.createQueue("DLQ");
      con.start();
      Message message = session.createConsumer(dlq).receive(50000);
      Assert.assertNotNull("expected DLQ msg is added", message);
      if (message != null) {
         Assert.assertTrue("expected exception request message is added to DLQ", ((TextMessage)message).getText().contains("exception"));
      }
  }
   //@Test
   @RunAsClient
   @org.junit.Ignore
   public void testJMSEndpointClientSide() throws Exception
   {
      URL wsdlUrl = getResourceURL("jaxws/cxf/jms/META-INF/wsdl/HelloWorldService.wsdl");
      QName serviceName = new QName("http://org.jboss.ws/jaxws/cxf/jms", "HelloWorldService");

      Service service = Service.create(wsdlUrl, serviceName);
      HelloWorld proxy = (HelloWorld) service.getPort(new QName("http://org.jboss.ws/jaxws/cxf/jms", "HelloWorldImplPort"), HelloWorld.class);
      setupProxy(proxy);
      try {
         assertEquals("Hi", proxy.echo("Hi"));
      } catch (Exception e) {
         rethrowAndHandleAuthWarning(e);
      }
      proxy.echo("exception");
   }
   
   private void setupProxy(HelloWorld proxy) {
      JMSConduit conduit = (JMSConduit)ClientProxy.getClient(proxy).getConduit();
      JMSConfiguration config = conduit.getJmsConfig();
      config.setUserName(JBossWSTestHelper.getTestUsername());
      config.setPassword(JBossWSTestHelper.getTestPassword());
      Properties props = conduit.getJmsConfig().getJndiEnvironment();
      props.put(Context.SECURITY_PRINCIPAL, JBossWSTestHelper.getTestUsername());
      props.put(Context.SECURITY_CREDENTIALS, JBossWSTestHelper.getTestPassword());
      props.put(Context.INITIAL_CONTEXT_FACTORY, getInitialContextFactory());
      props.put(Context.PROVIDER_URL, getRemotingProtocol() + "://" + getServerHost() + ":" + getServerPort(CXF_TESTS_GROUP_QUALIFIER, JMS_SERVER));

   }

   
   private static void rethrowAndHandleAuthWarning(Exception e) throws Exception {
      System.out.println("This test requires a testQueue JMS queue and a user with 'guest' role to be available on the application server; " +
            "queue are easily added using jboss-cli.sh/bat, while users are added using add-user.sh/bat. When running test please specify user " +
            "and password using -Dtest.username=\"foo\" -Dtest.password=\"bar\".");
      throw e;
   }
}
