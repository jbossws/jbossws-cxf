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

import java.net.URL;

import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.extensions.TestSetup;
import junit.framework.Test;

import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test JAXWS Endpoint deployment
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 29-Apr-2005
 */
public class EndpointTestCase extends JBossWSTest
{

   public static Test suite()
   {
      return new TestSetup(new JBossWSTestSetup(EndpointTestCase.class, "jaxws-endpoint-servlet.war jaxws-endpoint-ws.war"));
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
