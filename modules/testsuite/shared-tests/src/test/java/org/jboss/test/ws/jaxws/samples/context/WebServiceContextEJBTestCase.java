/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.test.ws.jaxws.samples.context;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
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
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/context/META-INF/jboss.xml"), "jboss.xml");
      return archive;
   }

   public void setUp() throws Exception {
      URL wsdlURL = new URL(baseURL + "/jaxws-samples-context?wsdl");
      QName qname = new QName("http://org.jboss.ws/jaxws/context", "EndpointService");
      Service service = Service.create(wsdlURL, qname);
      port = (Endpoint) service.getPort(Endpoint.class);

      BindingProvider bp = (BindingProvider) port;
      bp.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "kermit");
      bp.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "thefrog");
   }

   @Test
   @RunAsClient
   public void testGetWebContext() throws Exception
   {
      setUp();
      String retStr = port.testGetMessageContext();
      assertEquals("pass", retStr);
   }

   @Test
   @RunAsClient
   public void testMessageContextProperties() throws Exception
   {
      setUp();
      String retStr = port.testMessageContextProperties();
      assertEquals("pass", retStr);
   }

   @Test
   @RunAsClient
   public void testGetUserPrincipal() throws Exception
   {
      setUp();
      String retStr = port.testGetUserPrincipal();
      assertEquals("kermit", retStr);
   }

   @Test
   @RunAsClient
   public void testIsUserInRole() throws Exception
   {
      setUp();
      assertTrue("kermit is my friend", port.testIsUserInRole("friend"));
   }
}
