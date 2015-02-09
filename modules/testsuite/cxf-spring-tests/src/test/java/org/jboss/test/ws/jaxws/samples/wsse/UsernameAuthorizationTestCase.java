/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.cxf.binding.soap.saaj.SAAJOutInterceptor;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * WS-Security username authorization test case
 * 
 * @author Sergey Beryozkin
 *
 */
@RunWith(Arquillian.class)
public final class UsernameAuthorizationTestCase extends JBossWSTest
{
   private final String serviceURL = "http://" + getServerHost() + ":" + getServerPort()  + "/jaxws-samples-wsse-username-authorize/default-config";
   
   private final QName servicePort = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecurity", "SecurityServicePort");

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-wsse-username-authorize.war");
      archive
         .addManifest()
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.ServiceIface.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.ServiceImpl.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.jaxws.GreetMe.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.jaxws.GreetMeResponse.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.jaxws.SayHello.class)
         .addClass(org.jboss.test.ws.jaxws.samples.wsse.jaxws.SayHelloResponse.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/username-authorize/WEB-INF/jboss-web.xml"), "jboss-web.xml")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/username-authorize/WEB-INF/jbossws-cxf.xml"), "jbossws-cxf.xml")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/username-authorize/WEB-INF/wsdl/SecurityService.wsdl"), "wsdl/SecurityService.wsdl")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/username-authorize/WEB-INF/wsdl/SecurityService_schema1.xsd"), "wsdl/SecurityService_schema1.xsd")
         .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/username-authorize/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testAuthorized() throws Exception
   {
	   doTestAuthorized(serviceURL, servicePort, "kermit");
   }
   
   private void doTestAuthorized(String endpointAddress, QName portName, String userName) throws Exception
   {
      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecurity", "SecurityService");
      URL wsdlURL = new URL(endpointAddress + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(portName, ServiceIface.class);
      setupWsse(proxy, userName);
      assertEquals("Secure Hello World!", proxy.sayHello());
   }

   @Test
   @RunAsClient
   public void testUnauthenticated() throws Exception
   {
      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecurity", "SecurityService");
      URL wsdlURL = new URL(serviceURL + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(ServiceIface.class);
      setupWsse(proxy, "foo");
      try
      {
         proxy.sayHello();
         fail("User foo should not be authenticated.");
      }
      catch (Exception ex)
      {
         //expected
      }
   }

   @Test
   @RunAsClient
   public void testUnauthorized() throws Exception
   {
	   doTestUnauthorized(serviceURL, servicePort, "kermit");
   }
   
   private void doTestUnauthorized(String endpointAddress, QName portName, String userName) throws Exception
   {
      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecurity", "SecurityService");
      URL wsdlURL = new URL(endpointAddress + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(portName, ServiceIface.class);
      setupWsse(proxy, userName);
      try
      {
         proxy.greetMe();
         fail("User kermit should not be authorized to invoke greetMe.");
      }
      catch (Exception ex)
      {
         assertEquals("Unauthorized", ex.getMessage());
      }
   }
   
   private void setupWsse(ServiceIface proxy, String username)
   {
      Client client = ClientProxy.getClient(proxy);
      Endpoint cxfEndpoint = client.getEndpoint();

      Map<String, Object> outProps = new HashMap<String, Object>();
      outProps.put("action", "UsernameToken");
      outProps.put("user", username);
      outProps.put("passwordType", "PasswordText");
      outProps.put("passwordCallbackClass", "org.jboss.test.ws.jaxws.samples.wsse.UsernamePasswordCallback");
      WSS4JOutInterceptor wssOut = new WSS4JOutInterceptor(outProps); //request
      cxfEndpoint.getOutInterceptors().add(wssOut);
      cxfEndpoint.getOutInterceptors().add(new SAAJOutInterceptor());
   }
}
