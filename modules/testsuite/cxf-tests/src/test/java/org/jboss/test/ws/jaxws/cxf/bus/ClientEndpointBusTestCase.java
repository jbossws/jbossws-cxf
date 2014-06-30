/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.bus;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;

/**
 * A test case that verifies a client running inside and endpoint business method
 * does not use the deployment bus.
 * 
 * @author alessio.soldano@jboss.com
 * @since 13-Jun-2012
 *
 */
public class ClientEndpointBusTestCase extends JBossWSTest
{
   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-cxf-bus/ClientEndpointService/ClientEndpoint";
   
   public static Test suite()
   {
      return new JBossWSCXFTestSetup(ClientEndpointBusTestCase.class, DeploymentArchives.SERVER);
   }
   
   public void testClient() throws Exception
   {
      ClientEndpoint port = getPort();
      assertEquals("Foo", port.testClient("Foo", getServerHost()));
   }
   
   public void testCachedPort() throws Exception
   {
      ClientEndpoint port = getPort();
      assertEquals("Foo", port.testCachedPort("Foo", getServerHost()));
   }
   
   public void testCachedService() throws Exception
   {
      ClientEndpoint port = getPort();
      assertEquals("Foo", port.testCachedService("Foo", getServerHost()));
   }
   
   private ClientEndpoint getPort() throws Exception {
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/bus", "ClientEndpointService");
      Service service = Service.create(wsdlURL, serviceName);
      QName portQName = new QName("http://org.jboss.ws/bus", "ClientEndpointPort");
      return (ClientEndpoint) service.getPort(portQName, ClientEndpoint.class);
   }
}
