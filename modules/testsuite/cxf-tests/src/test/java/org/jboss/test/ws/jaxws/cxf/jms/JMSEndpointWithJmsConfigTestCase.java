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
import javax.jms.MessageListener;
import javax.jms.QueueConnectionFactory;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.xml.namespace.QName;

import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.ConduitInitiatorManager;
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
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test case for deploying an archive with a JMS (SOAP-over-JMS 1.0) endpoint and config with JMSConfigFeature on proxy
 *
 * @author ema@redhat.com
 */
@RunWith(Arquillian.class)
public class JMSEndpointWithJmsConfigTestCase extends JBossWSTest
{
   private static final String JMS_SERVER = "jms";
   private static volatile boolean waitForResponse;

   private static boolean useHornetQ() {
      return JBossWSTestHelper.isTargetWildFly9();
   }

   @Deployment(name="jaxws-cxf-jms-only-deployment-test-servlet", order=1, testable = false)
   @TargetsContainer(JMS_SERVER)
   public static WebArchive createWarDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-cxf-jms-only-deployment-test-servlet.war");
         archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client services," + (useHornetQ() ? "org.hornetq\n" : "org.apache.activemq.artemis")))
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jms/META-INF/permissions.xml"), "permissions.xml")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jms/META-INF/wsdl/HelloWorldService.wsdl"), "classes/META-INF/wsdl/HelloWorldService.wsdl")
               .addClass(org.jboss.test.ws.jaxws.cxf.jms.DeploymentTestServlet.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.jms.HelloWorld.class);
      return archive;
   }

   @Deployment(name="jaxws-cxf-jms-only-deployment", order=2, testable = false)
   @TargetsContainer(JMS_SERVER)
   public static JavaArchive createJarDeployment() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class,"jaxws-cxf-jms-only-deployment.jar");
         archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: " + (useHornetQ() ? "org.hornetq\n" : "org.apache.activemq.artemis")))
               .addClass(org.jboss.test.ws.jaxws.cxf.jms.HelloWorld.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.jms.HelloWorldImpl.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jms/META-INF/wsdl/HelloWorldService.wsdl"), "wsdl/HelloWorldService.wsdl");
      return archive;
   }

   @Test
   @RunAsClient
   //the test for setting jms configuration like sessionTransacted, transactionManager
   public void testJMSEndpointWithJmsConfigFeature() throws Exception
   {
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

      EndpointInfo endpointInfo = factory.getClientFactoryBean().getServiceFactory().getEndpointInfo();
      ConduitInitiatorManager cim = factory.getBus().getExtension(ConduitInitiatorManager.class);
      ConduitInitiator ci = cim.getConduitInitiator("http://cxf.apache.org/transports/jms");
      JMSConduit conduit = (JMSConduit)ci.getConduit(endpointInfo, factory.getBus());
      try {
          //assertEquals("Hi", greeter.echo("Hi"));
          greeter.echo("transaction");
          // Timeout exception should be thrown
      }catch (javax.xml.ws.soap.SOAPFaultException e) {
         //expected timeout exception
         assertTrue("Timeout exception is expected", e.getMessage().contains("Timeout"));
         e.printStackTrace();
      }
      finally {
          ((java.io.Closeable)greeter).close();
      }
  }

   public static class ResponseListener implements MessageListener
   {
      public String resMessage;

      public void onMessage(Message msg)
      {
         TextMessage textMessage = (TextMessage)msg;
         try
         {
            resMessage = textMessage.getText();
            textMessage.acknowledge();  
            waitForResponse = false;
         }
         catch (Throwable t)
         {
            t.printStackTrace();
         }
      }
   }
   
   
   private static void rethrowAndHandleAuthWarning(Exception e) throws Exception {
      System.out.println("This test requires a testQueue JMS queue and a user with 'guest' role to be available on the application server; " +
            "queue are easily added using jboss-cli.sh/bat, while users are added using add-user.sh/bat. When running test please specify user " +
            "and password using -Dtest.username=\"foo\" -Dtest.password=\"bar\".");
      throw e;
   }
}
