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
package org.jboss.test.ws.jaxws.samples.wsseEJB;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.apache.cxf.binding.soap.saaj.SAAJOutInterceptor;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;

/**
 * Secure EJB endpoint test
 *
 * @author sberyozk@jredhat.com
 */
public class WsseEjbTestCase extends JBossWSTest
{
   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-samples-wsseEJB/EjbEndpointService/EjbEndpoint";

   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.JarDeployment("jaxws-samples-wsseEJB.jar") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.samples.wsseEJB.EjbEndpoint.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsseEJB.EjbEndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsseEJB.GreetMe.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsseEJB.GreetMeResponse.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsseEJB.SayHello.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsseEJB.SayHelloResponse.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsseEJB.UsernamePasswordCallback.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsseEJB/META-INF/jboss.xml"), "jboss.xml")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsseEJB/META-INF/jbossws-cxf.xml"), "jbossws-cxf.xml");
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSCXFTestSetup(WsseEjbTestCase.class, JBossWSTestHelper.writeToFile(createDeployments()), true);
   }

   private EjbEndpoint getPort() throws Exception
   {
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/wsseEJB", "EjbEndpointService");
      EjbEndpoint port = Service.create(wsdlURL, serviceName).getPort(EjbEndpoint.class);
      return port;
   }

   public void testHello() throws Exception
   {
      EjbEndpoint proxy = getPort();
      setupWsse(proxy, "kermit");
      String retObj = proxy.sayHello();
      assertEquals("hello", retObj);
   }

   public void testGreetMeUnauthorized() throws Exception
   {
      EjbEndpoint proxy = getPort();
      setupWsse(proxy, "kermit");
      try
      {
          proxy.greetMe();
          fail("Unauthorized exception is expected");
      }
      catch (Exception ex)
      {
         assertTrue(ex.getMessage().contains("EjbEndpoint is not allowed"));
      }
   }

   private void setupWsse(EjbEndpoint proxy, String username)
   {
      Client client = ClientProxy.getClient(proxy);
      Endpoint cxfEndpoint = client.getEndpoint();

      Map<String, Object> outProps = new HashMap<String, Object>();
      outProps.put("action", "UsernameToken");
      outProps.put("user", username);
      outProps.put("passwordType", "PasswordText");
      outProps.put("passwordCallbackClass", "org.jboss.test.ws.jaxws.samples.wsseEJB.UsernamePasswordCallback");
      WSS4JOutInterceptor wssOut = new WSS4JOutInterceptor(outProps); //request
      cxfEndpoint.getOutInterceptors().add(wssOut);
      cxfEndpoint.getOutInterceptors().add(new SAAJOutInterceptor());
   }
}
