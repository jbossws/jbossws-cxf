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
package org.jboss.test.ws.jaxws.jbws1529;

import java.io.File;
import java.net.URL;

import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * wsdlReader fails with faults defined on jaxws SEI
 * 
 * http://jira.jboss.org/jira/browse/JBWS-1529
 *
 * @author Thomas.Diesler@jboss.com
 */
@RunWith(Arquillian.class)
public class JBWS1529TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws1529.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws1529.JBWS1529.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1529.JBWS1529Impl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1529.UserException.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1529/WEB-INF/web.xml"));
      return archive;
   }

   private static JBWS1529 proxy;

   @Before
   public void setup() throws Exception
   {
      if (proxy == null) {
         QName serviceName = new QName("http://jbws1529.jaxws.ws.test.jboss.org/", "JBWS1529Service");
         URL wsdlURL = new URL(baseURL + "/TestService?wsdl");
      
         Service service = Service.create(wsdlURL, serviceName);
         proxy = (JBWS1529)service.getPort(JBWS1529.class);
      }
   }

   @AfterClass
   public static void cleanup() throws Exception
   {
      proxy = null;
   }

   @Test
   @RunAsClient
   public void testWSDLReader() throws Exception
   {
      File wsdlFile = getResourceFile("jaxws/jbws1529/META-INF/wsdl/JBWS1529Service.wsdl");
      assertTrue(wsdlFile.exists());
      
      WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
      Definition wsdl = wsdlReader.readWSDL(wsdlFile.getAbsolutePath());
      assertNotNull(wsdl);
   }

   @Test
   @RunAsClient
   public void testEcho() throws Exception
   {
      String retStr = proxy.echo("hi there");
      assertEquals("hi there", retStr);
   }
}
