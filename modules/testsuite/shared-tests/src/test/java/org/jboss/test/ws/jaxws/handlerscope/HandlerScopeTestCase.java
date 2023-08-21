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
package org.jboss.test.ws.jaxws.handlerscope;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

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
 * Test SOAP12 binding type
 *
 * @author Thomas.Diesler@jboss.org
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 * @since 12-Aug-2006
 */
@RunWith(Arquillian.class)
public class HandlerScopeTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-handlerscope.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.handlerscope.GeneralServerHandler.class)
               .addClass(org.jboss.test.ws.jaxws.handlerscope.InvalidPortServerHandler.class)
               .addClass(org.jboss.test.ws.jaxws.handlerscope.InvalidServiceServerHandler.class)
               .addClass(org.jboss.test.ws.jaxws.handlerscope.PortServerHandler.class)
               .addClass(org.jboss.test.ws.jaxws.handlerscope.PortWildcardServerHandler.class)
               .addClass(org.jboss.test.ws.jaxws.handlerscope.ProtocolHandler.class)
               .addClass(org.jboss.test.ws.jaxws.handlerscope.SOAP11ServerHandler.class)
               .addClass(org.jboss.test.ws.jaxws.handlerscope.SOAP12ServerHandler.class)
               .addClass(org.jboss.test.ws.jaxws.handlerscope.SOAPEndpoint.class)
               .addClass(org.jboss.test.ws.jaxws.handlerscope.SOAPEndpointBean.class)
               .addClass(org.jboss.test.ws.jaxws.handlerscope.SOAPServerHandler.class)
               .addClass(org.jboss.test.ws.jaxws.handlerscope.ServiceServerHandler.class)
               .addClass(org.jboss.test.ws.jaxws.handlerscope.ServiceWildcardServerHandler.class)
               .addAsResource("org/jboss/test/ws/jaxws/handlerscope/jaxws-server-handlers.xml")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/handlerscope/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testClientAccess() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/jaxws/handlerscope", "SOAPEndpointService");
      Service service = new SOAPEndpointService(wsdlURL, serviceName);
      SOAPEndpoint port = (SOAPEndpoint)service.getPort(SOAPEndpoint.class);

      String retStr = port.echo("hello");
      assertResponse(retStr);
   }
   
   private static void assertResponse(String retStr)
   {
      StringBuffer expStr = new StringBuffer("hello");
      expStr.append(":SOAP12ClientHandler");
      expStr.append(":SOAPClientHandler");
      expStr.append(":ServiceClientHandler");
      expStr.append(":ServiceWildcardClientHandler");
      expStr.append(":PortClientHandler");
      expStr.append(":PortWildcardClientHandler");
      expStr.append(":GeneralClientHandler");
      expStr.append(":GeneralServerHandler");
      expStr.append(":PortWildcardServerHandler");
      expStr.append(":PortServerHandler");
      expStr.append(":ServiceWildcardServerHandler");
      expStr.append(":ServiceServerHandler");
      expStr.append(":SOAPServerHandler");
      expStr.append(":SOAP12ServerHandler");
      expStr.append(":endpoint");
      expStr.append(":SOAP12ServerHandler");
      expStr.append(":SOAPServerHandler");
      expStr.append(":ServiceServerHandler");
      expStr.append(":ServiceWildcardServerHandler");
      expStr.append(":PortServerHandler");
      expStr.append(":PortWildcardServerHandler");
      expStr.append(":GeneralServerHandler");
      expStr.append(":GeneralClientHandler");
      expStr.append(":PortWildcardClientHandler");
      expStr.append(":PortClientHandler");
      expStr.append(":ServiceWildcardClientHandler");
      expStr.append(":ServiceClientHandler");
      expStr.append(":SOAPClientHandler");
      expStr.append(":SOAP12ClientHandler");
      assertEquals(expStr.toString(), retStr);
   }
}
