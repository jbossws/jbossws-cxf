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
package org.jboss.test.ws.jaxws.jbws2307;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-2307] / [JBWS-2997] testcase
 * [JBWS-3820] JAXWS 2.1 / 2.0 clients and WebServiceRef using JAXWS features cause NoSuchMethodException
 * 
 * @author alessio.soldano@jboss.com
 */
@RunWith(Arquillian.class)
public class JBWS2307MTOMTestCase extends JBossWSTest
{
   public static final String SERVER_DEPLOYMENT = "jaxws-jbws2307-service";
   public static final String CLIENT_DEPLOYMENT = "jaxws-jbws2307-client";
   public static final String CLIENT_2_DEPLOYMENT = "jaxws-jbws2307-client-2";
   public static final String CLIENT_3_DEPLOYMENT = "jaxws-jbws2307-client-3";
   
   @ArquillianResource
   private URL baseURL;

   @ArquillianResource
   Deployer deployer;

   @Deployment(name = SERVER_DEPLOYMENT, order=1, testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, SERVER_DEPLOYMENT + ".war");
      archive
         .addManifest()
         .addClass(org.jboss.test.ws.jaxws.jbws2307.Hello.class)
         .addClass(org.jboss.test.ws.jaxws.jbws2307.HelloImpl.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-service/jboss-web.xml"), "jboss-web.xml")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-service/web.xml"), "web.xml")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-service/wsdl/HelloService.wsdl"), "wsdl/HelloService.wsdl")
         .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-service/web.xml"));
      return archive;
   }

   @Deployment(name = CLIENT_DEPLOYMENT, order=2, testable = false)
   public static WebArchive createDeployment4() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, CLIENT_DEPLOYMENT + ".war");
      archive
         .addManifest()
         .addClass(org.jboss.test.ws.jaxws.jbws2307.ClientServlet.class)
         .addClass(org.jboss.test.ws.jaxws.jbws2307.Hello.class)
         .addClass(org.jboss.test.ws.jaxws.jbws2307.HelloServiceJAXWS22.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-client/jboss-web.xml"), "jboss-web.xml")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-client/web.xml"), "web.xml")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-client/wsdl/HelloService.wsdl"), "wsdl/HelloService.wsdl")
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-client/permissions.xml"), "permissions.xml")
         .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-client/web.xml"));
      return archive;
   }

   @Deployment(name = CLIENT_3_DEPLOYMENT, testable = false, managed = false)
   public static WebArchive createDeployment3() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, CLIENT_3_DEPLOYMENT + ".war");
      archive
         .addManifest()
         .addClass(org.jboss.test.ws.jaxws.jbws2307.ClientServlet3.class)
         .addClass(org.jboss.test.ws.jaxws.jbws2307.Hello.class)
         .addClass(org.jboss.test.ws.jaxws.jbws2307.HelloServiceJAXWS22.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-client/jboss-web.xml"), "jboss-web.xml")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-client/web3.xml"), "web.xml")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-client/wsdl/HelloService.wsdl"), "wsdl/HelloService.wsdl")
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-client/permissions.xml"), "permissions.xml")
         .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-client/web3.xml"));
      return archive;
   }

   @Deployment(name = CLIENT_2_DEPLOYMENT, testable = false, managed = false)
   public static WebArchive createDeployment2() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, CLIENT_2_DEPLOYMENT + ".war");
      archive
         .addManifest()
         .addClass(org.jboss.test.ws.jaxws.jbws2307.ClientServlet2.class)
         .addClass(org.jboss.test.ws.jaxws.jbws2307.Hello.class)
         .addClass(org.jboss.test.ws.jaxws.jbws2307.HelloService.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-client/jboss-web.xml"), "jboss-web.xml")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-client/web2.xml"), "web.xml")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-client/wsdl/HelloService.wsdl"), "wsdl/HelloService.wsdl")
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-client/permissions.xml"), "permissions.xml")
         .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2307/WEB-INF-client/web2.xml"));
      return archive;
   }
   
   @Test
   @RunAsClient
   @OperateOnDeployment(CLIENT_DEPLOYMENT)
   public void testMTOM() throws Exception
   {
      assertEquals("true", IOUtils.readAndCloseStream(new URL("http://" + baseURL.getHost() + ":" + baseURL.getPort() + "/jaxws-jbws2307-client/jbws2307?mtom=true").openStream()));
   }

   @Test
   @RunAsClient
   @OperateOnDeployment(CLIENT_DEPLOYMENT)
   public void testClient() throws Exception
   {
      HttpURLConnection con = (HttpURLConnection)new URL("http://" + baseURL.getHost() + ":" + baseURL.getPort() + "/jaxws-jbws2307-client/jbws2307").openConnection();
      BufferedReader isr = new BufferedReader(new InputStreamReader(con.getInputStream()));
      assertEquals("true", isr.readLine());
   }

   @Test
   @RunAsClient
   @OperateOnDeployment(SERVER_DEPLOYMENT)
   public void testUsingClientArchive3() throws Exception
   {
      try {
         deployer.deploy(CLIENT_3_DEPLOYMENT);
         assertEquals("true", IOUtils.readAndCloseStream(new URL("http://" + baseURL.getHost() + ":" + baseURL.getPort() + "/jaxws-jbws2307-client-3/jbws2307?mtom=true").openStream()));
         HttpURLConnection con = (HttpURLConnection)new URL("http://" + baseURL.getHost() + ":" + baseURL.getPort() + "/jaxws-jbws2307-client-3/jbws2307").openConnection();
         BufferedReader isr = new BufferedReader(new InputStreamReader(con.getInputStream()));
         assertEquals("true", isr.readLine());
      } finally {
         deployer.undeploy(CLIENT_3_DEPLOYMENT);
      }
   }

   @Test
   @RunAsClient
   @OperateOnDeployment(SERVER_DEPLOYMENT)
   public void testUsingClientArchive2() throws Exception
   {
      try {
         deployer.deploy(CLIENT_2_DEPLOYMENT);
         URL url = new URL("http://" + baseURL.getHost() + ":" + baseURL.getPort() + "/jaxws-jbws2307-client-2/jbws2307?mtom=true");
         final HttpURLConnection c = (HttpURLConnection)url.openConnection();
         c.connect();
         assertEquals(500, c.getResponseCode());
         String error = IOUtils.readAndCloseStream(c.getErrorStream());
         c.disconnect();
         if (error.contains("error-text-div")) { //the actual error exception does not seem to be always returned by web layer
            assertTrue(error.contains("Could not instantiate ClientServlet2"));
         }
      } finally {
         deployer.undeploy(CLIENT_2_DEPLOYMENT);
      }
   }
}
