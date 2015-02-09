/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.spring;

import java.io.File;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * An application embedding Spring jars acts as a client to an existing WS endpoint.
 * This testcase verifies the spring availability in the app does not badly affect ws functionalities. 
 *
 * @author alessio.soldano@jboss.com
 * @since 02-Apr-2012
 */
@RunWith(Arquillian.class)
public final class ClientSpringAppTestCase extends JBossWSTest
{
   private static final String DEP = "jaxws-cxf-spring";
   private static final String CLIENT_DEP = "jaxws-cxf-spring-client";
   
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(name = DEP, testable = false)
   public static WebArchive createDeployment()
   {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, DEP + ".war");
      archive.addManifest().addClass(org.jboss.test.ws.jaxws.cxf.spring.Endpoint.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.spring.EndpointImpl.class);
      return archive;
   }
   
   @Deployment(name = CLIENT_DEP, testable = false)
   public static WebArchive createDeployment2()
   {
      final File SPRING_DIR = new File(new File(JBossWSTestHelper.getTestResourcesDir()).getParentFile(), "spring");
      WebArchive archive = ShrinkWrap.create(WebArchive.class, CLIENT_DEP + ".war");
      archive
         .setManifest(new StringAsset("Manifest-Version: 1.0\n" + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client services\n"))
         .addClass(org.jboss.test.ws.jaxws.cxf.spring.Endpoint.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.spring.Foo.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.spring.Helper.class)
         .addClass(org.jboss.wsf.test.ClientHelper.class)
         .addClass(org.jboss.wsf.test.TestServlet.class)
         .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/spring/my-cxf.xml"), "my-cxf.xml")
         .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/spring/spring-dd.xml"), "spring-dd.xml")
         .addAsLibrary(new File(SPRING_DIR, "spring-aop-3.0.3.RELEASE.jar"))
         .addAsLibrary(new File(SPRING_DIR, "spring-asm-3.0.3.RELEASE.jar"))
         .addAsLibrary(new File(SPRING_DIR, "spring-beans-3.0.3.RELEASE.jar"))
         .addAsLibrary(new File(SPRING_DIR, "spring-context-3.0.3.RELEASE.jar"))
         .addAsLibrary(new File(SPRING_DIR, "spring-core-3.0.3.RELEASE.jar"))
         .addAsLibrary(new File(SPRING_DIR, "spring-expression-3.0.3.RELEASE.jar"))
         .addAsLibrary(new File(SPRING_DIR, "spring-web-3.0.3.RELEASE.jar"));
      return archive;
   }
   
   @Test
   @RunAsClient
   @OperateOnDeployment(CLIENT_DEP)
   public void testSpringAvailability() throws Exception
   {
      assertEquals("1", runTestInContainer("testSpringAvailability", Helper.class.getName()));
   }

   @Test
   @RunAsClient
   @OperateOnDeployment(CLIENT_DEP)
   public void testJBossWSCXFSpringBus() throws Exception
   {
      assertEquals("1", runTestInContainer("testJBossWSCXFSpringBus", Helper.class.getName()));
   }

   @Test
   @RunAsClient
   @OperateOnDeployment(CLIENT_DEP)
   public void testJAXWSClient() throws Exception
   {
      assertEquals("1", runTestInContainer("testJAXWSClient", Helper.class.getName()));
   }

   @Test
   @RunAsClient
   @OperateOnDeployment(CLIENT_DEP)
   public void testSpringFunctionalities() throws Exception
   {
      assertEquals("1", runTestInContainer("testSpringFunctionalities", Helper.class.getName()));
   }

   private String runTestInContainer(String test, String helper) throws Exception
   {
      URL url = new URL(baseURL + "?path=/jaxws-cxf-spring/EndpointService&method="
            + test + "&helper=" + helper);
      return IOUtils.readAndCloseStream(url.openStream());
   }
}
