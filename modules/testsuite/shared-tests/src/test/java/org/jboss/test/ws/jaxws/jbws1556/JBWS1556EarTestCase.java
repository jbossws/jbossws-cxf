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
package org.jboss.test.ws.jaxws.jbws1556;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-1556] @WebWservice does not work with class isolation
 *
 * http://jira.jboss.org/jira/browse/JBWS-1556
 *
 * @author Thomas.Diesler@jboss.com
 * @since 15-Jun-2007
 */
@RunWith(Arquillian.class)
public class JBWS1556EarTestCase extends JBossWSTest
{
   private static EndpointInterface port;

   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static EnterpriseArchive createDeployment3() {
      JavaArchive archive1 = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws1556.jar");
         archive1
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws1556.EJB3Bean.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1556.UserType.class);
      JBossWSTestHelper.writeToFile(archive1);

      EnterpriseArchive archive = ShrinkWrap.create(EnterpriseArchive.class, "jaxws-jbws1556.ear");
         archive
               .addManifest()
               .addAsModule(new File(JBossWSTestHelper.getTestArchiveDir(), "jaxws-jbws1556.jar"))
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1556/META-INF/application.xml"), "application.xml");
      return archive;
   }

   @Before
   public void setup() throws MalformedURLException
   {
      if (port == null)
      {
         URL wsdlURL = new URL( baseURL + "/jaxws-jbws1556/EJB3Bean?wsdl");
         QName serviceName = new QName("http://jbws1556.jaxws.ws.test.jboss.org/", "EJB3BeanService");
         Service service = Service.create(wsdlURL, serviceName);
         port = service.getPort(EndpointInterface.class);
      }
   }
   
   @AfterClass
   public static void cleanup() {
      port = null;
   }

   @Test
   @RunAsClient
   public void testSimpleAccess() throws Exception
   {
      String hello = port.helloSimple("hello");
      assertEquals("hello", hello);
   }

   @Test
   @RunAsClient
   public void testComplexAccess() throws Exception
   {
      UserType req = new UserType("hello");
      UserType res = port.helloComplex(req);
      assertEquals(req, res);
   }
}
