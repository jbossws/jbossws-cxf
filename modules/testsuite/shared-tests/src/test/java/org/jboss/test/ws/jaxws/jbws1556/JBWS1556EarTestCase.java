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
