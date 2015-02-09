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
package org.jboss.test.ws.jaxws.jbws3140;

import static org.jboss.wsf.test.JBossWSTestHelper.getTestResourcesDir;

import java.io.File;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class JBWS3140TestCase extends JBossWSTest
{
   @ArquillianResource
   Deployer deployer;

   @Deployment(name="jbws3140-server", testable = false, managed=false)
   public static WebArchive createDeployment1() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jbws3140-server.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws3140.ClientHandler.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3140.DataType.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3140.MTOMTest.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3140.MTOMTestImpl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3140.ObjectFactory.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3140.ResponseType.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3140.ServerHandler.class)
               .addAsResource("org/jboss/test/ws/jaxws/jbws3140/client-handlers.xml")
               .addAsResource("org/jboss/test/ws/jaxws/jbws3140/server-handlers.xml")
               .addAsWebInfResource(new File(getTestResourcesDir() + "/jaxws/jbws3140/WEB-INF-Server/jboss-web.xml"), "jboss-web.xml")
               .addAsWebInfResource(new File(getTestResourcesDir() + "/jaxws/jbws3140/WEB-INF-Server/web.xml"), "web.xml")
               .addAsWebInfResource(new File(getTestResourcesDir() + "/jaxws/jbws3140/WEB-INF-Server/webservices.xml"), "webservices.xml")
               .setWebXML(new File(getTestResourcesDir() + "/jaxws/jbws3140/WEB-INF-Server/web.xml"));
      return archive;
   }

   @Deployment(name = "jbws3140-responses-server", testable = false, managed=false)
   public static WebArchive createDeployment2() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jbws3140-responses-server.war");
      archive
         .addManifest()
         .addClass(org.jboss.test.ws.jaxws.jbws3140.ClientHandler.class)
         .addClass(org.jboss.test.ws.jaxws.jbws3140.DataType.class)
         .addClass(org.jboss.test.ws.jaxws.jbws3140.EndpointService.class)
         .addClass(org.jboss.test.ws.jaxws.jbws3140.MTOMTest.class)
         .addClass(org.jboss.test.ws.jaxws.jbws3140.MTOMTestImpl.class)
         .addClass(org.jboss.test.ws.jaxws.jbws3140.ObjectFactory.class)
         .addClass(org.jboss.test.ws.jaxws.jbws3140.ResponseType.class)
         .addClass(org.jboss.test.ws.jaxws.jbws3140.ServerHandler.class)
         .addAsResource("org/jboss/test/ws/jaxws/jbws3140/client-handlers.xml")
         .addAsResource("org/jboss/test/ws/jaxws/jbws3140/server-handlers.xml")
         .addAsWebInfResource(new File(getTestResourcesDir() + "/jaxws/jbws3140/wsdl/TestEndpoint.wsdl"), "wsdl/TestEndpoint.wsdl")
         .addAsWebInfResource(new File(getTestResourcesDir() + "/jaxws/jbws3140/WEB-INF-Responses-Server/jboss-web.xml"), "jboss-web.xml")
         .addAsWebInfResource(new File(getTestResourcesDir() + "/jaxws/jbws3140/WEB-INF-Responses-Server/web.xml"), "web.xml")
         .addAsWebInfResource(new File(getTestResourcesDir() + "/jaxws/jbws3140/WEB-INF-Responses-Server/webservices.xml"), "webservices.xml")
         .setWebXML(new File(getTestResourcesDir() + "/jaxws/jbws3140/WEB-INF-Server/web.xml"));
      return archive;
   }

   @Deployment(name = "jbws3140-client", testable = false, managed=false)
   public static WebArchive createDeployment3() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jbws3140-client.war");
         archive
               .addManifest()
               .addAsResource(new File(getTestResourcesDir() + "/jaxws/jbws3140/large.jpg"))
               .addAsResource(new File(getTestResourcesDir() + "/jaxws/jbws3140/small.jpg"))
               .addClass(org.jboss.test.ws.jaxws.jbws3140.ClientHandler.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3140.DataType.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3140.EndpointService.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3140.MTOMTest.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3140.MTOMTestImpl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3140.ObjectFactory.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3140.ResponseType.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3140.ServerHandler.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3140.ServletTestClient.class)
               .addAsResource("org/jboss/test/ws/jaxws/jbws3140/client-handlers.xml")
               .addAsResource("org/jboss/test/ws/jaxws/jbws3140/server-handlers.xml")
               .addAsWebInfResource(new File(getTestResourcesDir() + "/jaxws/jbws3140/wsdl/TestEndpoint.wsdl"), "wsdl/TestEndpoint.wsdl")
               .addAsWebInfResource(new File(getTestResourcesDir() + "/jaxws/jbws3140/WEB-INF-Client/jboss-web.xml"), "jboss-web.xml")
               .setWebXML(new File(getTestResourcesDir() + "/jaxws/jbws3140/WEB-INF-Client/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testWsaResponses() throws Exception
   {
      try {
         deployer.deploy("jbws3140-responses-server");
         deployer.deploy("jbws3140-client");
         String result = IOUtils.readAndCloseStream(new URL("http://" + getServerHost() + ":" + getServerPort() + "/jbws3140-client/ServletTest" + "?mtom=small").openStream());
         assertTrue("SOAPFaultException is expected but received: " + result, result.indexOf("SOAPFaultException") > -1);
         String expectedDetail = "A header representing a Message Addressing Property is not valid";
         assertTrue("Expected message wasn't found in response: " + result, result.indexOf(expectedDetail) > -1);
      } finally {
         deployer.undeploy("jbws3140-responses-server");
         deployer.undeploy("jbws3140-client");
      }
   }

   @Test
   @RunAsClient
   public void testMtomSmall() throws Exception
   {
      try {
         deployer.deploy("jbws3140-server");
         deployer.deploy("jbws3140-client");
         String result = IOUtils.readAndCloseStream(new URL("http://" + getServerHost() + ":" + getServerPort() + "/jbws3140-client/ServletTest" + "?mtom=small").openStream());
         String expected ="--ClientMTOMEnabled--ServerMTOMEnabled--ServerAddressingEnabled--ClientAddressingEnabled";
         assertTrue("Expected string wasn't found in response: " + result, result.indexOf(expected) > -1);
      } finally {
         deployer.undeploy("jbws3140-server");
         deployer.undeploy("jbws3140-client");
      }
   }
}
