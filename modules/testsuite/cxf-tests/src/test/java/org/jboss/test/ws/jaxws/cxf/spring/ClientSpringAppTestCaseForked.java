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
 * This test is an addition to the org.jboss.test.ws.jaxws.cxf.spring.ClientSpringAppTestCase that runs
 * in forked mode as it requires setting sys props during test and hence can't be executed concurrently.
 *
 * WARN: this test uses the same deployment names of org.jboss.test.ws.jaxws.cxf.spring.ClientSpringAppTestCase;
 * this is not a problem only because this is a -Forked testcase, hence the two tests can't run at the same time. 
 *
 * @author alessio.soldano@jboss.com
 * @since 21-Jun-2013
 */
@RunWith(Arquillian.class)
public final class ClientSpringAppTestCaseForked extends JBossWSTest
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
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/spring/jboss-deployment-structure.xml"))
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/spring/permissions.xml"), "permissions.xml");
      JBossWSTestHelper.addLibrary(SPRING_DIR, archive);
      return archive;
   }
   
   @Test
   @RunAsClient
   @OperateOnDeployment(CLIENT_DEP)
   public void testJBossWSCXFBus() throws Exception
   {
      assertEquals("1", runTestInContainer("testJBossWSCXFBus", Helper.class.getName()));
   }

   private String runTestInContainer(String test, String helper) throws Exception
   {
      URL url = new URL(baseURL + "?path=/jaxws-cxf-spring/EndpointService&method="
            + test + "&helper=" + helper);
      return IOUtils.readAndCloseStream(url.openStream());
   }
}
