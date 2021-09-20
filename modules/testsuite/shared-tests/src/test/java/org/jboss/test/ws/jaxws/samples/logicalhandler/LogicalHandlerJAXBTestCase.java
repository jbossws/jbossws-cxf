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
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test JAXWS logical handlers
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 * @author Thomas.Diesler@jboss.org
 * @since 12-Aug-2006
 */
@RunWith(Arquillian.class)
public class LogicalHandlerJAXBTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-logicalhandler-jaxb.war");
         archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: com.sun.xml.bind export services\n"))
               .addClass(org.jboss.test.ws.jaxws.samples.logicalhandler.Echo.class)
               .addClass(org.jboss.test.ws.jaxws.samples.logicalhandler.EchoResponse.class)
               .addClass(org.jboss.test.ws.jaxws.samples.logicalhandler.LogicalJAXBHandler.class)
               .addClass(org.jboss.test.ws.jaxws.samples.logicalhandler.ObjectFactory.class)
               .addClass(org.jboss.test.ws.jaxws.samples.logicalhandler.PortHandler.class)
               .addClass(org.jboss.test.ws.jaxws.samples.logicalhandler.ProtocolHandler.class)
               .addClass(org.jboss.test.ws.jaxws.samples.logicalhandler.SOAPEndpointJAXB.class)
               .addClass(org.jboss.test.ws.jaxws.samples.logicalhandler.SOAPEndpointJAXBImpl.class)
               .addAsResource("org/jboss/test/ws/jaxws/samples/logicalhandler/jaxws-server-jaxb-handlers.xml")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/logicalhandler/WEB-INF/permissions.xml"), "permissions.xml")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/logicalhandler/WEB-INF/web-jaxb.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testClientAccess() throws Exception
   {
      String endpointAddress = baseURL.toString();
      QName serviceName = new QName("http://org.jboss.ws/jaxws/samples/logicalhandler", "SOAPEndpointService");
      Service service = new SOAPEndpointJAXBService(new URL(endpointAddress + "?wsdl"), serviceName);
      SOAPEndpointJAXB port = (SOAPEndpointJAXB)service.getPort(SOAPEndpointJAXB.class);

      BindingProvider bindingProvider = (BindingProvider)port;
      bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointAddress);

      String retStr = port.echo("hello");

      StringBuffer expStr = new StringBuffer("hello");
      expStr.append(":Outbound:LogicalJAXBHandler");
      expStr.append(":Outbound:ProtocolHandler");
      expStr.append(":Outbound:PortHandler");
      expStr.append(":Inbound:PortHandler");
      expStr.append(":Inbound:ProtocolHandler");
      expStr.append(":Inbound:LogicalJAXBHandler");
      expStr.append(":endpoint");
      expStr.append(":Outbound:LogicalJAXBHandler");
      expStr.append(":Outbound:ProtocolHandler");
      expStr.append(":Outbound:PortHandler");
      expStr.append(":Inbound:PortHandler");
      expStr.append(":Inbound:ProtocolHandler");
      expStr.append(":Inbound:LogicalJAXBHandler");
      assertEquals(expStr.toString(), retStr);
   }
}
