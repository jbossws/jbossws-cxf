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
package org.jboss.test.ws.jaxws.samples.logicalhandler;

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
 * Test JAXWS logical handlers
 *
 * @author Thomas.Diesler@jboss.org
 * @since 12-Aug-2006
 */
@RunWith(Arquillian.class)
public class LogicalHandlerSourceTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-logicalhandler-source.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.samples.logicalhandler.Echo.class)
               .addClass(org.jboss.test.ws.jaxws.samples.logicalhandler.EchoResponse.class)
               .addClass(org.jboss.test.ws.jaxws.samples.logicalhandler.LogicalSourceHandler.class)
               .addClass(org.jboss.test.ws.jaxws.samples.logicalhandler.PortHandler.class)
               .addClass(org.jboss.test.ws.jaxws.samples.logicalhandler.ProtocolHandler.class)
               .addClass(org.jboss.test.ws.jaxws.samples.logicalhandler.SOAPEndpointSourceDocImpl.class)
               .addClass(org.jboss.test.ws.jaxws.samples.logicalhandler.SOAPEndpointSourceRpcImpl.class)
               .addAsResource("org/jboss/test/ws/jaxws/samples/logicalhandler/jaxws-server-source-handlers.xml")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/logicalhandler/WEB-INF/web-source.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testSourceDoc() throws Exception
   {
      URL endpointAddress = new URL(baseURL + "/doc?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/jaxws/samples/logicalhandler", "SOAPEndpointDocService");
      Service service = new SOAPEndpointSourceService(endpointAddress, serviceName);
      SOAPEndpointSourceDoc port = (SOAPEndpointSourceDoc)service.getPort(SOAPEndpointSourceDoc.class);
      
      String retStr = port.echo("hello");
      assertResponse(retStr);
   }

   @Test
   @RunAsClient
   public void testSourceRpc() throws Exception
   {
      URL endpointAddress = new URL(baseURL + "/rpc?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/jaxws/samples/logicalhandler", "SOAPEndpointRpcService");
      Service service = new SOAPEndpointSourceService(endpointAddress, serviceName);
      SOAPEndpointSourceRpc port = (SOAPEndpointSourceRpc)service.getPort(SOAPEndpointSourceRpc.class);
      
      String retStr = port.echo("hello");
      assertResponse(retStr);
   }
   
   private void assertResponse(String retStr)
   {
      StringBuffer expStr = new StringBuffer("hello");
      expStr.append(":Outbound:LogicalSourceHandler");
      expStr.append(":Outbound:ProtocolHandler");
      expStr.append(":Outbound:PortHandler");
      expStr.append(":Inbound:PortHandler");
      expStr.append(":Inbound:ProtocolHandler");
      expStr.append(":Inbound:LogicalSourceHandler");
      expStr.append(":endpoint");
      expStr.append(":Outbound:LogicalSourceHandler");
      expStr.append(":Outbound:ProtocolHandler");
      expStr.append(":Outbound:PortHandler");
      expStr.append(":Inbound:PortHandler");
      expStr.append(":Inbound:ProtocolHandler");
      expStr.append(":Inbound:LogicalSourceHandler");
      assertEquals(expStr.toString(), retStr);
   }
}
