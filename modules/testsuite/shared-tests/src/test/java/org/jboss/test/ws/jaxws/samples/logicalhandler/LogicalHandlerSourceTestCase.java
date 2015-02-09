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
package org.jboss.test.ws.jaxws.samples.logicalhandler;

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
