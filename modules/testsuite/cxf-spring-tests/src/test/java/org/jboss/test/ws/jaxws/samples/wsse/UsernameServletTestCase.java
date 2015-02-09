/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.wsse;

import java.io.File;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * WS-Security username test case
 *
 * @author alessio.soldano@jboss.com
 * @since 22-Aug-2010
 */
@RunWith(Arquillian.class)
public final class UsernameServletTestCase extends JBossWSTest
{

   @Deployment(name="jaxws-samples-wsse-username2", order=1, testable = false)    //SERVER_WAR
   public static WebArchive createServerwar() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-wsse-username2.war");
      archive
         .setManifest(new StringAsset("Manifest-Version: 1.0\n"
            + "Dependencies: org.apache.ws.security\n"))
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.ServerUsernamePasswordCallback.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.ServiceIface.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.ServiceImpl.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.jaxws.GreetMe.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.jaxws.GreetMeResponse.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.jaxws.SayHello.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.jaxws.SayHelloResponse.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/username/WEB-INF/jbossws-cxf2.xml"), "jbossws-cxf.xml")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/username/WEB-INF/wsdl/SecurityService.wsdl"), "wsdl/SecurityService.wsdl")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/username/WEB-INF/wsdl/SecurityService_schema1.xsd"), "wsdl/SecurityService_schema1.xsd")
         .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/username/WEB-INF/web.xml"));
      return archive;
   }

   @Deployment(name="jaxws-samples-wsse-username-client2", order=2, testable = false)
   public static WebArchive createClientWar() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-wsse-username-client2.war");
      archive
         .setManifest(new StringAsset("Manifest-Version: 1.0\n"
            + "Dependencies: org.apache.ws.security,org.jboss.ws.cxf.jbossws-cxf-client services,org.apache.cxf.impl\n"))
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.ServiceIface.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.UsernameHelper.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.UsernamePasswordCallback.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.jaxws.GreetMe.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.jaxws.GreetMeResponse.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.jaxws.SayHello.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.jaxws.SayHelloResponse.class)
         .addClass(org.jboss.wsf.test.ClientHelper.class)
         .addClass(org.jboss.wsf.test.TestServlet.class);
      return archive;
   }

   @Test
   @RunAsClient
   public void test() throws Exception
   {
      assertEquals("1", runTestInContainer("test"));
   }

   @Test
   @RunAsClient
   public void testWrongPassword() throws Exception
   {
      assertEquals("1", runTestInContainer("testWrongPassword"));
   }
   
   private String runTestInContainer(String test) throws Exception
   {
      URL url = new URL("http://" + getServerHost() + ":" + getServerPort()
            + "/jaxws-samples-wsse-username-client2?path=/jaxws-samples-wsse-username2&method=" + test
            + "&helper=" + UsernameHelper.class.getName());
      return IOUtils.readAndCloseStream(url.openStream());
   }
}
