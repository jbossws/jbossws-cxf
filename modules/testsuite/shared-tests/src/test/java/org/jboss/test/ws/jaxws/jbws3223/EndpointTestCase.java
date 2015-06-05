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
package org.jboss.test.ws.jaxws.jbws3223;

import java.io.File;
import java.net.URL;

import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

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
 * [JBWS-3223] Runtime ws client classloader setup (on AS 7)
 * 
 * @author alessio.soldano@jboss.com
 * @since 18-Feb-2011
 */
@RunWith(Arquillian.class)
public class EndpointTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(name = "jaxws-jbws3223-servlet", order = 1, testable = false)
   public static WebArchive createDeployment1() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws3223-servlet.war");
      archive
         .setManifest(new StringAsset("Manifest-Version: 1.0\n"
            + "Dependencies: org.jboss.ws.common\n"))
         .addClass(org.jboss.test.ws.jaxws.jbws3223.Client.class)
         .addClass(org.jboss.test.ws.jaxws.jbws3223.EndpointInterface.class)
         .addClass(org.jboss.test.ws.jaxws.jbws3223.TestServlet.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3223/WEB-INF/wsdl/TestService.wsdl"), "wsdl/TestService.wsdl")
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3223/WEB-INF/permissions.xml"), "permissions.xml")
         .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3223/WEB-INF/web.xml"));
      return archive;
   }

   @Deployment(name = "jaxws-jbws3223", order = 2, testable = false)
   public static WebArchive createDeployment2() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws3223.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws3223.EndpointBean.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3223.EndpointInterface.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3223/WEB-INF/web-ws.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-jbws3223")
   public void testWSDLAccess() throws Exception
   {
      readWSDL(new URL(baseURL + "?wsdl"));
   }

   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-jbws3223")
   public void testClientAccess() throws Exception
   {
      String helloWorld = "Hello world!";
      Client client = new Client(false);
      Object retObj = client.run(helloWorld, getResourceURL("jaxws/jbws3223/WEB-INF/wsdl/TestService.wsdl"));
      assertEquals(helloWorld, retObj);
   }

   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-jbws3223-servlet")
   public void testServletAccess() throws Exception
   {
      URL url = new URL(baseURL + "?param=hello-world&clCheck=true");
      assertEquals("hello-world", IOUtils.readAndCloseStream(url.openStream()));
   }
   
   private void readWSDL(URL wsdlURL) throws Exception
   {
      WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
      Definition wsdlDefinition = wsdlReader.readWSDL(wsdlURL.toString());
      assertNotNull(wsdlDefinition);
   }

}
