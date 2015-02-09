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
package org.jboss.test.ws.jaxws.jbws1841;

import java.io.File;
import java.net.URL;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

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

   private void cleanUp() {
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

   protected void setUp() throws Exception {
      URL wsdlURL = new URL(baseURL + "/jaxws-jbws1841/EndpointService/EJB3Bean?wsdl");
      QName serviceName = new QName("http://www.openuri.org/2004/04/HelloWorld", "EndpointService");
      port = Service.create(wsdlURL, serviceName).getPort(EndpointInterface.class);

      ctx = getServerInitialContext();
      remote = (StatelessRemote) ctx.lookup("jaxws-jbws1841//" + StatelessBean.class.getSimpleName() + "!" + StatelessRemote.class.getName());
   }

   @Test
   @RunAsClient
   public void testDirectWSInvocation() throws Exception
   {
      setUp();
      String result = port.echo("DirectWSInvocation");
      assertEquals("DirectWSInvocation", result);
      cleanUp();
   }

   @Test
   @RunAsClient
   public void testEJBRelay1() throws Exception
   {
      setUp();
      String result = remote.echo1("Relay1");
      assertEquals("Relay1", result);
      cleanUp();
   }

   @Test
   @RunAsClient
   public void testEJBRelay2() throws Exception
   {
      setUp();
      String result = remote.echo2("Relay2");
      assertEquals("Relay2", result);
      cleanUp();
   }

   @Test
   @RunAsClient
   public void testEJBRelay3() throws Exception
   {
      setUp();
      String result = remote.echo3("Relay3");
      assertEquals("Relay3", result);
      cleanUp();
   }

   @Test
   @RunAsClient
   public void testEJBRelay4() throws Exception
   {
      setUp();
      String result = remote.echo4("Relay4");
      assertEquals("Relay4", result);
      cleanUp();
   }

}
