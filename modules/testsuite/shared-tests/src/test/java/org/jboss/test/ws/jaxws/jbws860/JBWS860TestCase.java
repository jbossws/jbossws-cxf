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
package org.jboss.test.ws.jaxws.jbws860;

import java.io.File;
import java.net.URL;

import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import org.jboss.arquillian.container.test.api.Deployment;
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
 * [JBWS-860] Investigate "Is Not A Servlet" error with multiple servlets in the web.xml
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 26-Apr-2006
 */
@RunWith(Arquillian.class)
public class JBWS860TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws860.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws860.InventoryWebService.class)
               .addClass(org.jboss.test.ws.jaxws.jbws860.UserServlet.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws860/WEB-INF/jboss-web.xml"), "jboss-web.xml")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws860/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testAccessInventoryServiceWsdl() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/InventoryWebService?wsdl");
      WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
      Definition wsdlDefinition = wsdlReader.readWSDL(wsdlURL.toString());
      assertNotNull(wsdlDefinition);
   }

   @Test
   @RunAsClient
   public void testServletAccess() throws Exception
   {     
      assertTrue(IOUtils.readAndCloseStream(new URL(baseURL + "/TestServlet").openStream()).startsWith("Hello"));
   }
}
