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
package org.jboss.test.ws.jaxws.jbws3282;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test the handlers (pre/post) declaration in jaxws endpoint configuration
 * 
 * https://issues.jboss.org/browse/JBWS-3282
 * https://issues.jboss.org/browse/JBWS-3836
 *
 * @author alessio.soldano@jboss.com
 * @since 03-May-2011
 */
@RunWith(Arquillian.class)
public class HandlerChainTestCase extends JBossWSTest
{
   private final String targetNS = "http://jbws3282.jaxws.ws.test.jboss.org/";

   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws3282.war");
         archive
            .addManifest()
            .addClass(org.jboss.test.ws.jaxws.jbws3282.AuthorizationHandler.class)
            .addClass(org.jboss.test.ws.jaxws.jbws3282.Endpoint.class)
            .addClass(org.jboss.test.ws.jaxws.jbws3282.EndpointHandler.class)
            .addClass(org.jboss.test.ws.jaxws.jbws3282.EndpointImpl.class)
            .addClass(org.jboss.test.ws.jaxws.jbws3282.Endpoint4Impl.class)
            .addClass(org.jboss.test.ws.jaxws.jbws3282.Endpoint5Impl.class)
            .addClass(org.jboss.test.ws.jaxws.jbws3282.Endpoint6Impl.class)
            .addClass(org.jboss.test.ws.jaxws.jbws3282.LogHandler.class)
            .addClass(org.jboss.test.ws.jaxws.jbws3282.RoutingHandler.class)
            .addAsResource("org/jboss/test/ws/jaxws/jbws3282/jaxws-handlers-server.xml")
            .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3282/WEB-INF/jaxws-endpoint-config.xml"))
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3282/WEB-INF/my-endpoint-config.xml"), "my-endpoint-config.xml")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3282/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testHandlerChain() throws Exception
   {
      QName serviceName = new QName(targetNS, "EndpointImplService");
      URL wsdlURL = new URL(baseURL + "/ep?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      Endpoint port = (Endpoint)service.getPort(Endpoint.class);

      String resStr = port.echo("Kermit");
      assertEquals("Kermit|RoutIn|AuthIn|EpIn|LogIn|endpoint|LogOut|EpOut|AuthOut|RoutOut", resStr);
   }

   /**
    * [JBWS-3836] Test endpoint configuration from default file and named as the endpoint impl class 
    * 
    * @throws Exception
    */
   @Test
   @RunAsClient
   public void testHandlerChain4() throws Exception
   {
      QName serviceName = new QName(targetNS, "Endpoint4ImplService");
      URL wsdlURL = new URL(baseURL + "/ep4?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      Endpoint port = (Endpoint)service.getPort(Endpoint.class);

      String resStr = port.echo("Kermit");
      assertEquals("Kermit|RoutIn|EpIn|LogIn|endpoint4|LogOut|EpOut|RoutOut", resStr);
   }

   /**
    * [JBWS-3836] Test endpoint configuration from custom file and named as the endpoint impl class
    *             @EndpointConfig(configFile = "WEB-INF/my-endpoint-config.xml")
    * 
    * @throws Exception
    */
   @Test
   @RunAsClient
   public void testHandlerChain5() throws Exception
   {
      QName serviceName = new QName(targetNS, "Endpoint5ImplService");
      URL wsdlURL = new URL(baseURL + "/ep5?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      Endpoint port = (Endpoint)service.getPort(Endpoint.class);

      String resStr = port.echo("Kermit");
      assertEquals("Kermit|EpIn|LogIn|endpoint5|LogOut|EpOut", resStr);
   }

   /**
    * [JBWS-3836] Test endpoint configuration from default file and with a specified name
    *             @EndpointConfig(configName = "EP6-config")
    * 
    * @throws Exception
    */
   @Test
   @RunAsClient
   public void testHandlerChain6() throws Exception
   {
      QName serviceName = new QName(targetNS, "Endpoint6ImplService");
      URL wsdlURL = new URL(baseURL + "/ep6?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      Endpoint port = (Endpoint)service.getPort(Endpoint.class);

      String resStr = port.echo("Kermit");
      assertEquals("Kermit|AuthIn|EpIn|LogIn|endpoint6|LogOut|EpOut|AuthOut", resStr);
   }
}
