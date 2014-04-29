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
package org.jboss.test.ws.jaxws.cxf.udp;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Enumeration;

import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;

/**
 * Test case for publishing a UDP (SOAP-over-UDP 1.1) endpoint through API
 *
 * @author alessio.soldano@jboss.com
 * @since 6-May-2013
 */
public final class UDPEndpointAPITestCase extends JBossWSTest
{
   public static Test suite()
   {
      return new JBossWSCXFTestSetup(UDPEndpointAPITestCase.class, "jaxws-cxf-udp-api.war,jaxws-cxf-udp-api-client.jar");
   }
   
   public void testServerSide() throws Exception
   {
      URL url = new URL("http://" + getServerHost() + ":8080/jaxws-cxf-udp-api");
      assertEquals("true", IOUtils.readAndCloseStream(url.openStream()));
   }
   
   public void testClientSide() throws Exception
   {
      if (!isProperNetworkSetup()) {
         System.out.println("Skipping broadcast test: no non-loopback IPv4 interface available"); //IPv6 does not support broadcast, so some IPv4 nonloopback interface with broacast is required
         return;
      }
      Bus bus = BusFactory.newInstance().createBus();
      BusFactory.setThreadDefaultBus(bus);
      Object implementor = new HelloWorldImpl();
      Endpoint ep = Endpoint.publish("soap.udp://:9436", implementor);
      try
      {
         final QName serviceName = new QName("http://org.jboss.ws/jaxws/cxf/udp", "HelloWorldService");
         final QName udpPortName = new QName("http://org.jboss.ws/jaxws/cxf/udp", "UDPHelloWorldImplPort");
         Service service = Service.create(serviceName);
         service.addPort(udpPortName, "http://schemas.xmlsoap.org/soap/", "soap.udp://:9436");
         HelloWorld proxy = (HelloWorld) service.getPort(udpPortName, HelloWorld.class);
         assertEquals("Hi", proxy.echo("Hi"));
      }
      finally
      {
         ep.stop();
         bus.shutdown(true);
      }
   }
   
   private boolean isProperNetworkSetup() throws Exception {
      Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
      while (interfaces.hasMoreElements())
      {
         NetworkInterface networkInterface = interfaces.nextElement();
         if (networkInterface.isUp() && !networkInterface.isLoopback() && isBroadcastAddressAvailable(networkInterface))
         {
            return true;
         }
      }
      return false;
   }
   
   private boolean isBroadcastAddressAvailable(NetworkInterface networkInterface) {
      for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
         InetAddress broadcast = interfaceAddress.getBroadcast();
         if (broadcast != null) {
             return true;
         }
      }
      return false;
   }
   
}
