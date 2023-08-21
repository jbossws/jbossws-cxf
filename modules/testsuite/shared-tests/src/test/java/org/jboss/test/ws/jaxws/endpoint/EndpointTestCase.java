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
package org.jboss.test.ws.jaxws.endpoint;

import java.io.File;
import java.net.URL;

import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

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
 * Test JAXWS Endpoint deployment
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 29-Apr-2005
 */
@RunWith(Arquillian.class)
public class EndpointTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(name="jaxws-endpoint-servlet", testable = false)
   public static WebArchive createDeployment1() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-endpoint-servlet.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.endpoint.EndpointBean.class)
               .addClass(org.jboss.test.ws.jaxws.endpoint.EndpointInterface.class)
               .addClass(org.jboss.test.ws.jaxws.endpoint.EndpointServlet.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/endpoint/WEB-INF/wsdl/TestService.wsdl"), "wsdl/TestService.wsdl")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/endpoint/WEB-INF/permissions.xml"), "permissions.xml")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/endpoint/WEB-INF/web.xml"));
      return archive;
   }

   @Deployment(name="jaxws-endpoint-ws", testable = false)
   public static WebArchive createDeployment2() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-endpoint-ws.war");
      archive
         .addManifest()
         .addClass(org.jboss.test.ws.jaxws.endpoint.EndpointBean.class)
         .addClass(org.jboss.test.ws.jaxws.endpoint.EndpointInterface.class)
         .addClass(org.jboss.test.ws.jaxws.endpoint.WSClientEndpointBean.class)
         .addClass(org.jboss.test.ws.jaxws.endpoint.WSClientEndpointInterface.class)
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/endpoint/META-INF/permissions.xml"), "permissions.xml")
         .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/endpoint/WEB-INF/web-ws.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-endpoint-servlet")
   public void testWSDLAccess() throws Exception
   {
      readWSDL(new URL("http://" + baseURL.getHost() + ":8082/jaxws-endpoint?wsdl"));
      readWSDL(new URL("http://" + baseURL.getHost() + ":8082/jaxws-endpoint2/endpoint/long/path?wsdl"));
   }

   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-endpoint-servlet")
   public void testClientAccess() throws Exception
   {
      // Create the port
      URL wsdlURL = getResourceURL("jaxws/endpoint/WEB-INF/wsdl/TestService.wsdl");
      QName qname = new QName("http://org.jboss.ws/jaxws/endpoint", "EndpointService");
      Service service = Service.create(wsdlURL, qname);
      EndpointInterface port = (EndpointInterface)service.getPort(EndpointInterface.class);

      String helloWorld = "Hello world!";
      Object retObj = port.echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }

   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-endpoint-servlet")
   public void testServletAccess() throws Exception
   {
      URL url = new URL("http://" + baseURL.getHost() + ":" + baseURL.getPort() + "/jaxws-endpoint-servlet/?param=hello-world");
      assertEquals("hello-world", IOUtils.readAndCloseStream(url.openStream()));
   }

   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-endpoint-ws")
   public void testWSAccess() throws Exception
   {
      QName qname = new QName("http://org.jboss.ws/jaxws/endpoint", "WSClientEndpointService");
      Service service = Service.create(new URL(baseURL + "?wsdl"), qname);
      WSClientEndpointInterface port = (WSClientEndpointInterface)service.getPort(WSClientEndpointInterface.class);

      String helloWorld = "Hello world!";
      Object retObj = port.echo(helloWorld, getServerHost(), 9796);
      assertEquals(helloWorld, retObj);
      retObj = port.echo2(helloWorld);
      assertEquals(helloWorld, retObj);
   }

   private void readWSDL(URL wsdlURL) throws Exception
   {
      WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
      Definition wsdlDefinition = wsdlReader.readWSDL(wsdlURL.toString());
      assertNotNull(wsdlDefinition);
   }

}
