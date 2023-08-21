/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.test.ws.jaxws.jbws3287;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Verifies deployment descriptor support for jbossws-config-file / jbossws-config-name
 * 
 * https://issues.jboss.org/browse/JBWS-3287
 *
 * @author alessio.soldano@jboss.com
 * @since 25-May-2012
 */
@RunWith(Arquillian.class)
public class JBWS3287TestCase extends JBossWSTest
{
   private static final String targetNS = "http://jbws3287.jaxws.ws.test.jboss.org/";

   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false, name = "depC", order = 1)
   public static JavaArchive createDeploymentC() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws3287-C.jar");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws3287.AuthorizationHandler.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3287.EJB3EndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3287.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3287.EndpointHandler.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3287.LogHandler.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3287.RoutingHandler.class)
               .addAsResource("org/jboss/test/ws/jaxws/jbws3287/jaxws-handlers-server.xml")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3287/META-INF/jaxws-endpoint-config.xml"), "jaxws-endpoint-config.xml")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3287/META-INF/jboss-webservices.xml"), "jboss-webservices.xml");
      return archive;
   }

   @Deployment(testable = false,name = "depB", order = 2)
   public static WebArchive createDeploymentB() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws3287-B.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws3287.AuthorizationHandler.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3287.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3287.EndpointHandler.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3287.EndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3287.LogHandler.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3287.RoutingHandler.class)
               .addAsResource("org/jboss/test/ws/jaxws/jbws3287/jaxws-handlers-server.xml")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3287/WEB-INF/jaxws-endpoint-config.xml"), "jaxws-endpoint-config.xml")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3287/WEB-INF/web-B.xml"));
      return archive;
   }

   @Deployment(testable = false, name = "depA", order = 3)
   public static WebArchive createDeploymentA() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws3287-A.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws3287.AuthorizationHandler.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3287.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3287.EndpointHandler.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3287.EndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3287.LogHandler.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3287.RoutingHandler.class)
               .addAsResource("org/jboss/test/ws/jaxws/jbws3287/jaxws-handlers-server.xml")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3287/WEB-INF/jaxws-endpoint-config.xml"), "jaxws-endpoint-config.xml")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3287/WEB-INF/jboss-webservices.xml"), "jboss-webservices.xml")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3287/WEB-INF/web-A.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   @OperateOnDeployment("depA")
   public void testJBossWebservicesXmlDD() throws Exception
   {
      runTestInternal("jaxws-jbws3287-A/TestService?wsdl");
   }

   @Test
   @RunAsClient
   @OperateOnDeployment("depB")
   public void testWebXmlDD() throws Exception
   {
      runTestInternal("jaxws-jbws3287-B/TestService?wsdl");
   }

   @Test
   @RunAsClient
   @OperateOnDeployment("depC")
   public void testEJB3JBossWebservicesXmlDD() throws Exception
   {
      runTestInternal("jaxws-jbws3287-C/EndpointImplService/Endpoint?wsdl");
   }
   
   private void runTestInternal(String path) throws Exception {
      QName serviceName = new QName(targetNS, "EndpointImplService");
      URL wsdlURL = new URL("http://" + getServerHost() + ":" + getServerPort() + "/" + path);

      Service service = Service.create(wsdlURL, serviceName);
      Endpoint port = (Endpoint)service.getPort(Endpoint.class);

      String resStr = port.echo("Kermit");
      assertEquals("Kermit|RoutIn|AuthIn|EpIn|LogIn|endpoint|LogOut|EpOut|AuthOut|RoutOut", resStr);
   }
}
