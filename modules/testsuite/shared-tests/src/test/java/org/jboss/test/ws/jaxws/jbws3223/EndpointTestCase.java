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
package org.jboss.test.ws.jaxws.jbws3223;

import java.io.File;
import java.net.URL;

import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-3223] Runtime ws client classloader setup (on AS 7)
 * 
 * @author alessio.soldano@jboss.com
 * @since 18-Feb-2011
 */
@RunWith(Arquillian.class)
public class EndpointTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(name = "jaxws-jbws3223-servlet", order = 1, testable = false)
   public static WebArchive createDeployment1() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws3223-servlet.war");
      archive
         .setManifest(new StringAsset("Manifest-Version: 1.0\n"
            + "Dependencies: org.jboss.ws.common\n"))
         .addClass(org.jboss.test.ws.jaxws.jbws3223.Client.class)
         .addClass(org.jboss.test.ws.jaxws.jbws3223.EndpointInterface.class)
         .addClass(org.jboss.test.ws.jaxws.jbws3223.TestServlet.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3223/WEB-INF/wsdl/TestService.wsdl"), "wsdl/TestService.wsdl")
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3223/WEB-INF/permissions.xml"), "permissions.xml")
         .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3223/WEB-INF/web.xml"));
      return archive;
   }

   @Deployment(name = "jaxws-jbws3223", order = 2, testable = false)
   public static WebArchive createDeployment2() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws3223.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws3223.EndpointBean.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3223.EndpointInterface.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3223/WEB-INF/web-ws.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-jbws3223")
   public void testWSDLAccess() throws Exception
   {
      readWSDL(new URL(baseURL + "?wsdl"));
   }

   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-jbws3223")
   public void testClientAccess() throws Exception
   {
      String helloWorld = "Hello world!";
      Client client = new Client(false);
      Object retObj = client.run(helloWorld, getResourceURL("jaxws/jbws3223/WEB-INF/wsdl/TestService.wsdl"));
      assertEquals(helloWorld, retObj);
   }

   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-jbws3223-servlet")
   public void testServletAccess() throws Exception
   {
      URL url = new URL(baseURL + "?param=hello-world&clCheck=true");
      assertEquals("hello-world", IOUtils.readAndCloseStream(url.openStream()));
   }
   
   private void readWSDL(URL wsdlURL) throws Exception
   {
      WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
      Definition wsdlDefinition = wsdlReader.readWSDL(wsdlURL.toString());
      assertNotNull(wsdlDefinition);
   }

}
