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
package org.jboss.test.ws.jaxws.jbws1582;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

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
 * [JBWS-1582] Protect JBossWS Against XML Attacks
 *
 * @author <a href="mailto:richard.opalka@jboss.org">Richard Opalka</a>
 */
@RunWith(Arquillian.class)
public class JBWS1582TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @ArquillianResource
   Deployer deployer;
   
   @Deployment(name="jaxws-jbws1582-attacked", testable = false, managed=false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws1582-attacked.war");
      archive
         .addManifest()
         .addClass(org.jboss.test.ws.jaxws.jbws1582.AttackedEndpointImpl.class)
         .addClass(org.jboss.test.ws.jaxws.jbws1582.Endpoint.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1582/WEB-INF/wsdl/attack-service.wsdl"), "wsdl/attack-service.wsdl")
         .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1582/WEB-INF/attack-web.xml"));
      return archive;
   }

   @Deployment(name="jaxws-jbws1582", testable = false)
   public static WebArchive createDeployment1() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws1582.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws1582.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1582.EndpointImpl.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1582/WEB-INF/wsdl/service.wsdl"), "wsdl/service.wsdl")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1582/WEB-INF/web.xml"));
      return archive;
   }

   private String targetNS = "http://jbws1582.jaxws.ws.test.jboss.org/";

   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-jbws1582")
   public void testLegalAccess() throws Exception
   {
      String endpointURL = "http://" + baseURL.getHost() + ":" + baseURL.getPort() + "/jaxws-jbws1582/TestService";
      URL wsdlURL = new URL(endpointURL + "?wsdl");
      QName serviceName = new QName(targetNS, "EndpointService");

      Service service = Service.create(wsdlURL, serviceName);
      Endpoint port = (Endpoint)service.getPort(Endpoint.class);

      Object retObj = port.echo("Hello");
      assertEquals("Hello", retObj);
   }

   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-jbws1582")
   public void testSOAPMessage() throws Exception
   {
      String response = getResponse("jaxws/jbws1582/message.xml");
      assertTrue(response.contains("HTTP/1.1 200 OK") || response.contains("HTTP/1.0 200 OK"));
      assertTrue(response.contains("<return>Hello</return>"));
   }

   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-jbws1582")
   public void testSOAPMessageAttack1() throws Exception
   {
      String response = getResponse("jaxws/jbws1582/attack-message-1.xml");
      assertTrue(response.contains("HTTP/1.1 500") || response.contains("HTTP/1.0 500"));
      if (isIntegrationCXF())
      {
         assertTrue(response.contains("Error reading XMLStreamReader"));
      }
      else
      {
         throw new IllegalStateException("Unknown SOAP stack in use");
      }
   }

   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-jbws1582")
   public void testSOAPMessageAttack2() throws Exception
   {
      String response = getResponse("jaxws/jbws1582/attack-message-2.xml");
      assertTrue(response.contains("HTTP/1.1 500") || response.contains("HTTP/1.0 500"));
      if (isIntegrationCXF())
      {
         assertTrue(response.contains("Error reading XMLStreamReader"));
      }
      else
      {
         throw new IllegalStateException("Unknown SOAP stack in use");
      }
   }

   private String getResponse(String requestFile) throws Exception
   {
      final String CRNL = "\r\n";
      String content = getContent(new FileInputStream(getResourceFile(requestFile)));
      Socket socket = new Socket();
      socket.connect(new InetSocketAddress(getServerHost(), getServerPort()));
      OutputStream out = socket.getOutputStream();

      // send an HTTP request to the endpoint
      out.write(("POST /jaxws-jbws1582/TestService HTTP/1.0" + CRNL).getBytes());
      out.write(("Host: " + getServerHost() + ":" + getServerPort() + CRNL).getBytes());
      out.write(("Content-Type: text/xml" + CRNL).getBytes());
      out.write(("Content-Length: " + content.length() + CRNL).getBytes());
      out.write((CRNL).getBytes());
      out.write((content).getBytes());

      // read the response
      String response = getContent(socket.getInputStream());
      socket.close();
      System.out.println("---");
      System.out.println(response);
      System.out.println("---");
      return response;
   }


   @Test
   @RunAsClient
   public void testAttackedArchiveDeployment() throws Exception
   {
      try
      {
         deployer.deploy("jaxws-jbws1582-attacked");
         if (isIntegrationCXF())
         {
            // Apache CXF ignores DOCTYPE section in WSDLs
            // so this attack is not doable on it.
         }
         else
         {
            // JBossWS Native stack throws exception for attacking WSDLs
            fail("deployment failure expected");
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
         log.warn(e.getMessage(), e);
      }
      finally
      {
         deployer.undeploy("jaxws-jbws1582-attacked");
      }
   }

   private static String getContent(InputStream is) throws IOException
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      IOUtils.copyStream(baos, is);
      return new String(baos.toByteArray());
   }

}
