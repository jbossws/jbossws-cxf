/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.test.ws.jaxws.samples.handlerchain;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.handler.Handler;

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
 * Test the JSR-181 annotation: jakarta.jws.HandlerChain
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 * @author Thomas.Diesler@jboss.org
 * @since 15-Oct-2005
 */
@RunWith(Arquillian.class)
public class HandlerChainTestCase extends JBossWSTest
{
   private static final String targetNS = "http://handlerchain.samples.jaxws.ws.test.jboss.org/";

   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-handlerchain.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.samples.handlerchain.AuthorizationHandler.class)
               .addClass(org.jboss.test.ws.jaxws.samples.handlerchain.ClientMimeHandler.class)
               .addClass(org.jboss.test.ws.jaxws.samples.handlerchain.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.samples.handlerchain.EndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.samples.handlerchain.LogHandler.class)
               .addClass(org.jboss.test.ws.jaxws.samples.handlerchain.RoutingHandler.class)
               .addClass(org.jboss.test.ws.jaxws.samples.handlerchain.ServerMimeHandler.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/handlerchain/WEB-INF/permissions.xml"), "permissions.xml")
               .addAsResource("org/jboss/test/ws/jaxws/samples/handlerchain/jaxws-handlers-server.xml")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/handlerchain/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testDynamicHandlerChain() throws Exception
   {
      QName serviceName = new QName(targetNS, "EndpointImplService");
      URL wsdlURL = new URL(baseURL + "/TestService?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      Endpoint port = service.getPort(Endpoint.class);

      BindingProvider bindingProvider = (BindingProvider)port;
      @SuppressWarnings("rawtypes")
      List<Handler> handlerChain = new ArrayList<Handler>();
      handlerChain.add(new LogHandler());
      handlerChain.add(new AuthorizationHandler());
      handlerChain.add(new RoutingHandler());
      handlerChain.add(new ClientMimeHandler());
      bindingProvider.getBinding().setHandlerChain(handlerChain);

      String resStr = port.echo("Kermit");
      assertEquals("Kermit|LogOut|AuthOut|RoutOut|RoutIn|AuthIn|LogIn|endpoint|LogOut|AuthOut|RoutOut|RoutIn|AuthIn|LogIn", resStr);
      assertCookies();
   }

   @Test
   @RunAsClient
   public void testHandlerChainOnService() throws Exception
   {
      QName serviceName = new QName(targetNS, "EndpointImplService");
      URL wsdlURL = new URL(baseURL + "/TestService?wsdl");

      Service service = new EndpointWithHandlerChainService(wsdlURL, serviceName);
      EndpointWithHandlerChain port = service.getPort(EndpointWithHandlerChain.class);

      String resStr = port.echo("Kermit");
      assertEquals("Kermit|LogOut|AuthOut|RoutOut|RoutIn|AuthIn|LogIn|endpoint|LogOut|AuthOut|RoutOut|RoutIn|AuthIn|LogIn", resStr);
      assertCookies();
   }

   private void assertCookies() throws Exception
   {
      assertEquals("server-cookie=true", ClientMimeHandler.inboundCookie);
   }
}
