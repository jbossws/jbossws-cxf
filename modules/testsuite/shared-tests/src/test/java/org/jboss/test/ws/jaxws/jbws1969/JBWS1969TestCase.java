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
package org.jboss.test.ws.jaxws.jbws1969;

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
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-1969] test case for load/import of resources under nested directory
 * other than toplevel "META-INF/wsdl" directory
 * @author <a href="mailto:mageshbk@jboss.com">Magesh Kumar B</a>
 */
@RunWith(Arquillian.class)
public class JBWS1969TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static JavaArchive createDeployments() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws1969.jar");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws1969.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1969.EndpointImpl.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1969/META-INF/wsdl/echo/TestService.wsdl"), "wsdl/echo/TestService.wsdl")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1969/META-INF/wsdl/echo/TestService.xsd"), "wsdl/echo/TestService.xsd");
      return archive;
   }

   @Test
   @RunAsClient
   public void testSubDirectory() throws Exception
   {
      // WSDL and Schema files are loaded in META-INF/wsdl/echo directory
      URL wsdlURL = new URL(baseURL + "/jaxws-jbws1969?wsdl");
      WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
      Definition wsdlDefinition = wsdlReader.readWSDL(wsdlURL.toString());
      assertNotNull(wsdlDefinition);
   }
}
