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
package org.jboss.test.ws.jaxws.samples.webparam;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Holder;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test the JSR-181 annotation: jakarta.jws.WebParam
 *
 * @author Thomas.Diesler@jboss.org
 * @since 07-Oct-2005
 */
@RunWith(Arquillian.class)
public class WebParamTestCase extends JBossWSTest
{
   private String targetNS = "http://www.openuri.org/jsr181/WebParamExample";  
   private static PingService port;

   @Deployment(testable = false)
   public static WebArchive createDeployment1() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-webparam.war");
      archive.addManifest()
             .addClass(org.jboss.test.ws.jaxws.samples.webparam.PingDocument.class)
             .addClass(org.jboss.test.ws.jaxws.samples.webparam.PingServiceImpl.class)
             .addClass(org.jboss.test.ws.jaxws.samples.webparam.SecurityHeader.class)
             .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/webparam/WEB-INF/web.xml"));
      return archive;
   }

   @Before
   public void createPort() throws Exception
   {
      if (port == null)
      {
         QName serviceName = new QName(targetNS, "PingServiceService");
         URL wsdlURL = getResourceURL("jaxws/samples/webparam/META-INF/wsdl/PingService.wsdl");

         Service service = Service.create(wsdlURL, serviceName);
         port = service.getPort(PingService.class);
      }
   }
   @Test
   @RunAsClient
   public void testEcho() throws Exception
   {
      PingDocument doc = new PingDocument();
      doc.setContent("Hello Kermit");
      PingDocument retObj = port.echo(doc);
      assertEquals(doc.getContent(), retObj.getContent());
   }
   @Test
   @RunAsClient
   public void testPingOneWay() throws Exception
   {
      PingDocument doc = new PingDocument();
      doc.setContent("Hello Kermit");
      port.pingOneWay(doc);
   }
   @Test
   @RunAsClient
   public void testPingTwoWay() throws Exception
   {
      PingDocument doc = new PingDocument();
      doc.setContent("Hello Kermit");
      Holder<PingDocument> holder = new Holder<PingDocument>(doc);

      port.pingTwoWay(holder);
      assertEquals("Hello Kermit Response", holder.value.getContent());
   }
   @Test
   @RunAsClient
   public void testSecurePing() throws Exception
   {
      PingDocument doc = new PingDocument();
      doc.setContent("Hello Kermit");
      SecurityHeader secHeader = new SecurityHeader();
      secHeader.setValue("some secret");

      port.securePing(doc, secHeader);
   }
   
   @AfterClass
   public static void cleanupPort() {
	   port = null;
   }
}
