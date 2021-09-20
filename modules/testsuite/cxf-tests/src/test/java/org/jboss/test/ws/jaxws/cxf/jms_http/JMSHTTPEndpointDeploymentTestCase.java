/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.jms_http;

import java.io.File;
import java.net.URL;
import java.util.Properties;

import javax.naming.Context;
import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.jms.JMSConduit;
import org.apache.cxf.transport.jms.JMSConfiguration;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.ws.jaxws.cxf.jms.HelloWorld;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test case for deploying an archive with a JMS (SOAP-over-JMS 1.0) and a HTTP endpoints 
 *
 * @author alessio.soldano@jboss.com
 * @since 10-Jun-2011
 */
@RunWith(Arquillian.class)
public final class JMSHTTPEndpointDeploymentTestCase extends JBossWSTest
{
   private static final String JMS_SERVER = "jms";
   
   private static boolean useHornetQ() {
      return JBossWSTestHelper.isTargetWildFly9();
   }

   @Deployment(name = "jaxws-cxf-jms-http-deployment", order = 1, testable = false)
   @TargetsContainer(JMS_SERVER)
   public static WebArchive createDeployment1()
   {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-cxf-jms-http-deployment.war");
      archive
            .setManifest(new StringAsset("Manifest-Version: 1.0\nDependencies: " + (useHornetQ() ? "org.hornetq\n" : "org.apache.activemq.artemis\n")))
            .addClass(org.jboss.test.ws.jaxws.cxf.jms_http.HelloWorld.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jms_http.HelloWorldImpl.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jms_http.HttpHelloWorldImpl.class)
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jms_http/WEB-INF/wsdl/HelloWorldService.wsdl"), "wsdl/HelloWorldService.wsdl")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jms_http/WEB-INF/web.xml"));
      return archive;
   }

   @Deployment(name = "jaxws-cxf-jms-http-deployment-test-servlet", order = 2, testable = false)
   @TargetsContainer(JMS_SERVER)
   public static WebArchive createDeployment2()
   {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-cxf-jms-http-deployment-test-servlet.war");
      archive
            .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                        + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client services," + (useHornetQ() ? "org.hornetq\n" : "org.apache.activemq.artemis")))
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jms_http/WEB-INF/wsdl/HelloWorldService.wsdl"), "classes/META-INF/wsdl/HelloWorldService.wsdl")
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jms_http/WEB-INF/permissions.xml"), "permissions.xml")
            .addClass(org.jboss.test.ws.jaxws.cxf.jms_http.DeploymentTestServlet.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jms_http.HelloWorld.class);
      return archive;
   }

   @Test
   @RunAsClient
   public void testJMSEndpointServerSide() throws Exception
   {
      URL url = new URL("http://" + getServerHost() + ":" + getServerPort(CXF_TESTS_GROUP_QUALIFIER, JMS_SERVER) + "/jaxws-cxf-jms-http-deployment-test-servlet");
      assertEquals("true", IOUtils.readAndCloseStream(url.openStream()));
   }

   @Test
   @RunAsClient
   public void testJMSEndpointClientSide() throws Exception
   {
      URL wsdlUrl = getResourceURL("jaxws/cxf/jms_http/WEB-INF/wsdl/HelloWorldService.wsdl");
      QName serviceName = new QName("http://org.jboss.ws/jaxws/cxf/jms", "HelloWorldService");

      Service service = Service.create(wsdlUrl, serviceName);
      HelloWorld proxy = (HelloWorld) service.getPort(new QName("http://org.jboss.ws/jaxws/cxf/jms", "HelloWorldImplPort"), HelloWorld.class);
      setupProxy(proxy);
      try
      {
         assertEquals("Hi", proxy.echo("Hi"));
      }
      catch (Exception e)
      {
         System.out.println("This test requires a testQueue JMS queue and a user with 'guest' role to be available on the application server; "
                     + "queue are easily added using jboss-cli.sh/bat, while users are added using add-user.sh/bat. When running test please specify user "
                     + "and password using -Dtest.username=\"foo\" -Dtest.password=\"bar\".");
         throw e;
      }
   }

   @Test
   @RunAsClient
   public void testHTTPEndpointClientSide() throws Exception
   {
      URL wsdlUrl = new URL("http://" + getServerHost() + ":" + getServerPort(CXF_TESTS_GROUP_QUALIFIER, JMS_SERVER) + "/jaxws-cxf-jms-http-deployment?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/jaxws/cxf/jms", "HelloWorldService");

      Service service = Service.create(wsdlUrl, serviceName);
      HelloWorld proxy = (HelloWorld) service.getPort(new QName("http://org.jboss.ws/jaxws/cxf/jms", "HttpHelloWorldImplPort"), HelloWorld.class);
      assertEquals("(http) Hi", proxy.echo("Hi"));
   }

   private void setupProxy(HelloWorld proxy)
   {
      JMSConduit conduit = (JMSConduit) ClientProxy.getClient(proxy).getConduit();
      JMSConfiguration config = conduit.getJmsConfig();
      config.setUserName(JBossWSTestHelper.getTestUsername());
      config.setPassword(JBossWSTestHelper.getTestPassword());
      Properties props = conduit.getJmsConfig().getJndiEnvironment();
      props.put(Context.SECURITY_PRINCIPAL, JBossWSTestHelper.getTestUsername());
      props.put(Context.SECURITY_CREDENTIALS, JBossWSTestHelper.getTestPassword());
   }
}
