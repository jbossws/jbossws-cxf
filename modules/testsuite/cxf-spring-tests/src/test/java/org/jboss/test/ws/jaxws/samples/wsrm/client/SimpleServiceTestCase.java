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
package org.jboss.test.ws.jaxws.samples.wsrm.client;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.cxf.endpoint.Client;
import org.jboss.test.ws.jaxws.samples.wsrm.generated.SimpleService;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;

/**
 * Client invoking web service using WS-RM
 *
 * @author richard.opalka@jboss.com
 */
@RunWith(Arquillian.class)
public final class SimpleServiceTestCase extends JBossWSTest
{
   private final String serviceURL = "http://" + getServerHost()  + ":" + getServerPort() + "/jaxws-samples-wsrm/SimpleService";
   private SimpleService proxy;

   @Deployment(name = "jaxws-samples-wsrm", order = 1, testable = false)
   public static WebArchive createDeployment1() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-wsrm.war");
      archive
         .addManifest()
         .addClass(org.jboss.test.ws.jaxws.samples.wsrm.service.SimpleServiceImpl.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsrm.service.jaxws.Echo.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsrm.service.jaxws.EchoResponse.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsrm.service.jaxws.Ping.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsrm/WEB-INF/wsdl/SimpleService.wsdl"), "wsdl/SimpleService.wsdl")
         .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsrm/WEB-INF/web.xml"));
      return archive;
   }

   @Deployment(name = "jaxws-samples-wsrm-client", order = 2, testable = false)
   public static JavaArchive createDeployment2() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-samples-wsrm-client.jar");
      archive
         .addManifest()
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsrm/cxf.xml"), "cxf.xml");
      return archive;
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wsrm", "SimpleService");
      URL wsdlURL = new URL(serviceURL + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      proxy = (SimpleService)service.getPort(SimpleService.class);
   }
   
   @Override
   protected void tearDown() throws Exception
   {
      if (proxy != null) {
         ((Client)proxy).destroy();
      }
   }

   @Test
   @RunAsClient
   public void test() throws Exception
   {
      setUp();
      assertEquals("Hello World!", proxy.echo("Hello World!")); // request responce call
      proxy.ping(); // one way call
      tearDown();
   }
   
}
