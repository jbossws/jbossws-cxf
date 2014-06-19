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
package org.jboss.test.ws.jaxws.endpoint;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.extensions.TestSetup;
import junit.framework.Test;

import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test JAXWS Endpoint deployment
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 29-Apr-2005
 */
public class EndpointTestCase extends JBossWSTest
{
   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-endpoint-servlet.war") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.endpoint.EndpointBean.class)
               .addClass(org.jboss.test.ws.jaxws.endpoint.EndpointInterface.class)
               .addClass(org.jboss.test.ws.jaxws.endpoint.EndpointServlet.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/endpoint/WEB-INF/wsdl/TestService.wsdl"), "wsdl/TestService.wsdl")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/endpoint/WEB-INF/web.xml"));
         }
      });
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-endpoint-ws.war") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.endpoint.EndpointBean.class)
               .addClass(org.jboss.test.ws.jaxws.endpoint.EndpointInterface.class)
               .addClass(org.jboss.test.ws.jaxws.endpoint.WSClientEndpointBean.class)
               .addClass(org.jboss.test.ws.jaxws.endpoint.WSClientEndpointInterface.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/endpoint/META-INF/permissions.xml"), "permissions.xml")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/endpoint/WEB-INF/web-ws.xml"));
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new TestSetup(new JBossWSTestSetup(EndpointTestCase.class, JBossWSTestHelper.writeToFile(createDeployments())));
   }

   public void testWSDLAccess() throws Exception
   {
      readWSDL(new URL("http://" + getServerHost() + ":8081/jaxws-endpoint?wsdl"));
      readWSDL(new URL("http://" + getServerHost() + ":8081/jaxws-endpoint2/endpoint/long/path?wsdl"));
   }
   
   public void testClientAccess() throws Exception
   {
      // Create the port
      URL wsdlURL = getResourceURL("jaxws/endpoint/WEB-INF/wsdl/TestService.wsdl");
      QName qname = new QName("http://org.jboss.ws/jaxws/endpoint", "EndpointService");
      Service service = Service.create(wsdlURL, qname);
      EndpointInterface port = (EndpointInterface)service.getPort(EndpointInterface.class);

      String helloWorld = "Hello world!";
      Object retObj = port.echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }

   public void testServletAccess() throws Exception
   {
      URL url = new URL("http://" + getServerHost() + ":8080/jaxws-endpoint-servlet?param=hello-world");
      assertEquals("hello-world", IOUtils.readAndCloseStream(url.openStream()));
   }
   
   public void testWSAccess() throws Exception
   {
      QName qname = new QName("http://org.jboss.ws/jaxws/endpoint", "WSClientEndpointService");
      Service service = Service.create(new URL("http://" + getServerHost() + ":8080/jaxws-endpoint-ws?wsdl"), qname);
      WSClientEndpointInterface port = (WSClientEndpointInterface)service.getPort(WSClientEndpointInterface.class);

      String helloWorld = "Hello world!";
      Object retObj = port.echo(helloWorld, getServerHost(), 9796);
      assertEquals(helloWorld, retObj);
      retObj = port.echo2(helloWorld);
      assertEquals(helloWorld, retObj);
   }

   private void readWSDL(URL wsdlURL) throws Exception
   {
      WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
      Definition wsdlDefinition = wsdlReader.readWSDL(wsdlURL.toString());
      assertNotNull(wsdlDefinition);
   }

}
