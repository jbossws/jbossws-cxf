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
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test JAXWS logical handlers
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 * @author Thomas.Diesler@jboss.org
 * @since 12-Aug-2006
 */
public class LogicalHandlerJAXBTestCase extends JBossWSTest
{
   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-samples-logicalhandler-jaxb.war") { {
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
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/logicalhandler/WEB-INF/web-jaxb.xml"));
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSTestSetup(LogicalHandlerJAXBTestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
   }

   public void testClientAccess() throws Exception
   {
      String endpointAddress = "http://" + getServerHost() + ":8080/jaxws-samples-logicalhandler-jaxb";
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
