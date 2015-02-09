/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.in_container_client;
import java.io.File;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-3832] Different default Spring descriptor name
 * for creating client and server Bus instances
 *
 */
@RunWith(Arquillian.class)
public class CustomBusServletTestCaseForked extends JBossWSTest
{
   @Deployment(name = "jaxws-cxf-in_container_client-client_spring", order = 1, testable = false)
   public static WebArchive createDeployment1() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-cxf-in_container_client-client_spring.war");
      archive
         .addManifest()
         .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/in_container_client/cxf.xml"), "cxf.xml")
         .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/in_container_client/cxf-client.xml"), "cxf-client.xml")
         .addClass(org.jboss.test.ws.jaxws.cxf.in_container_client.HelloWorld.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.in_container_client.Helper.class)
         .addClass(org.jboss.wsf.test.ClientHelper.class)
         .addClass(org.jboss.wsf.test.TestServlet.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/in_container_client/WEB-INF/jboss-deployment-structure.xml"), "jboss-deployment-structure.xml");
      return archive;
   }

   @Deployment(name = "jaxws-cxf-in_container_client_spring", order = 2, testable = false)
   public static WebArchive createDeployment2() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-cxf-in_container_client_spring.war");
      archive
         .addManifest()
         .addClass(org.jboss.test.ws.jaxws.cxf.in_container_client.HelloWorld.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.in_container_client.HelloWorldImpl.class);
      return archive;
   }

   @Test
   @RunAsClient
   public void test() throws Exception
   {
      setUp();
      assertEquals("1", runTestInContainer("testSpringBus"));
      tearDown();
   }
   
   private String runTestInContainer(String test) throws Exception
   {
      URL url = new URL("http://" + getServerHost() + ":" + getServerPort()
         + "/jaxws-cxf-in_container_client-client_spring?path=/jaxws-cxf-in_container_client_spring/HelloWorldService&method=" + test
            + "&helper=" + Helper.class.getName());
      return IOUtils.readAndCloseStream(url.openStream());
   }
}
