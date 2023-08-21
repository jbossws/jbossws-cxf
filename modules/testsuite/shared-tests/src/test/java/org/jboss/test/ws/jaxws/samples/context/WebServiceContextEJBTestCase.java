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
package org.jboss.test.ws.jaxws.samples.context;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test JAXWS WebServiceContext
 *
 * @author Thomas.Diesler@jboss.org
 * @since 29-Apr-2005
 */
@RunWith(Arquillian.class)
public class WebServiceContextEJBTestCase extends JBossWSTest
{
   private static Endpoint port;

   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static JavaArchive createDeployments() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-samples-context.jar");
         archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: org.jboss.ws.common\n"))
               .addClass(org.jboss.test.ws.jaxws.samples.context.EndpointEJB.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/context/META-INF/permissions.xml"), "permissions.xml")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/context/META-INF/jboss.xml"), "jboss.xml");
      return archive;
   }

   @Before
   public void setup() throws Exception {
      if (port == null) {
         URL wsdlURL = new URL(baseURL + "/jaxws-samples-context?wsdl");
         QName qname = new QName("http://org.jboss.ws/jaxws/context", "EndpointService");
         Service service = Service.create(wsdlURL, qname);
         port = (Endpoint) service.getPort(Endpoint.class);
   
         BindingProvider bp = (BindingProvider) port;
         bp.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "kermit");
         bp.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "thefrog");
      }
   }
   
   @AfterClass
   public static void clean() {
      port = null;
   }

   @Test
   @RunAsClient
   public void testGetWebContext() throws Exception
   {
      String retStr = port.testGetMessageContext();
      assertEquals("pass", retStr);
   }

   @Test
   @RunAsClient
   public void testMessageContextProperties() throws Exception
   {
      String retStr = port.testMessageContextProperties();
      assertEquals("pass", retStr);
   }

   @Test
   @RunAsClient
   public void testGetUserPrincipal() throws Exception
   {
      String retStr = port.testGetUserPrincipal();
      assertEquals("kermit", retStr);
   }

   @Test
   @RunAsClient
   public void testIsUserInRole() throws Exception
   {
      assertTrue("kermit is my friend", port.testIsUserInRole("friend"));
   }
}
