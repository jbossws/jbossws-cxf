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
package org.jboss.test.ws.jaxws.jbws1841;

import java.io.File;
import java.net.URL;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Serviceref through ejb3 deployment descriptor.
 *
 * http://jira.jboss.org/jira/browse/JBWS-1841
 *
 * @author Heiko.Braun@jboss.com
 */
@RunWith(Arquillian.class)
public class JBWS1841TestCase extends JBossWSTest
{
   private static EndpointInterface port;
   private static StatelessRemote remote;
   private static InitialContext ctx;

   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static JavaArchive createDeployments() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws1841.jar");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws1841.EJB3Bean.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1841.EndpointInterface.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1841.EndpointService.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1841.StatelessBean.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1841.StatelessRemote.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1841/META-INF/ejb-jar.xml"), "ejb-jar.xml")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1841/META-INF/jboss-ejb3.xml"), "jboss-ejb3.xml")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1841/META-INF/jboss-webservices.xml"), "jboss-webservices.xml")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1841/META-INF/jboss.xml"), "jboss.xml")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1841/META-INF/permissions.xml"), "permissions.xml")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1841/META-INF/wsdl/TestService.wsdl"), "wsdl/TestService.wsdl");
      return archive;
   }

   @AfterClass
   public static void cleanUp() {
      port = null;
      remote = null;
      if (ctx != null) {
         final InitialContext c = ctx;
         ctx = null;
         try {
            c.close();
         } catch (NamingException ne) {
            throw new RuntimeException(ne);
         }
      }
   }

   @Before
   public void setup() throws Exception {
      if (port == null) {
         URL wsdlURL = new URL(baseURL + "/jaxws-jbws1841/EndpointService/EJB3Bean?wsdl");
         QName serviceName = new QName("http://www.openuri.org/2004/04/HelloWorld", "EndpointService");
         port = Service.create(wsdlURL, serviceName).getPort(EndpointInterface.class);
   
         ctx = getServerInitialContext();
         remote = (StatelessRemote) ctx.lookup("jaxws-jbws1841//" + StatelessBean.class.getSimpleName() + "!" + StatelessRemote.class.getName());
      }
   }

   @Test
   @RunAsClient
   public void testDirectWSInvocation() throws Exception
   {
      String result = port.echo("DirectWSInvocation");
      assertEquals("DirectWSInvocation", result);
   }

   @Test
   @RunAsClient
   public void testEJBRelay1() throws Exception
   {
      String result = remote.echo1("Relay1");
      assertEquals("Relay1", result);
   }

   @Test
   @RunAsClient
   public void testEJBRelay2() throws Exception
   {
      String result = remote.echo2("Relay2");
      assertEquals("Relay2", result);
   }

   @Test
   @RunAsClient
   public void testEJBRelay3() throws Exception
   {
      String result = remote.echo3("Relay3");
      assertEquals("Relay3", result);
   }

   @Test
   @RunAsClient
   public void testEJBRelay4() throws Exception
   {
      String result = remote.echo4("Relay4");
      assertEquals("Relay4", result);
   }

}
