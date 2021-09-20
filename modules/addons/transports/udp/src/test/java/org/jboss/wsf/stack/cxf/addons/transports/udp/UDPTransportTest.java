/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf.addons.transports.udp;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;

public class UDPTransportTest
{
   private static final String PORT = "9434";
   private static Server server;
   private static Bus bus;

   @BeforeClass
   public static void setUpBeforeClass() throws Exception
   {
      bus = BusFactory.newInstance().createBus();
      BusFactory.setDefaultBus(bus);
      JaxWsServerFactoryBean factory = new JaxWsServerFactoryBean();
      factory.setBus(bus);
      factory.setAddress("udp://:" + PORT);
      factory.setServiceBean(new GreeterImpl());
      server = factory.create();
   }

   @AfterClass
   public static void shutdown() throws Exception
   {
      server.stop();
      server = null;
      if (bus != null) {
         bus.shutdown(true);
      }
      bus = null;
   }

   @Test
   public void testSimpleUDP() throws Exception
   {
      JaxWsProxyFactoryBean fact = new JaxWsProxyFactoryBean();
      fact.setAddress("udp://localhost:" + PORT);
      Greeter g = fact.create(Greeter.class);
      for (int x = 0; x < 5; x++)
      {
         Assert.assertEquals("Hello World", g.greetMe("World"));
      }

      ((java.io.Closeable) g).close();
   }

   @Test
   public void testBroadcastUDP() throws Exception
   {
      Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
      int count = 0;
      while (interfaces.hasMoreElements())
      {
         NetworkInterface networkInterface = interfaces.nextElement();
         if (!networkInterface.isUp() || networkInterface.isLoopback() || !isBroadcastAddressAvailable(networkInterface))
         {
            continue;
         }
         count++;
      }
      if (count == 0)
      {
         //no non-loopbacks, cannot do broadcasts
         System.out.println("Skipping broadcast test: no non-loopback IPv4 interface available");
         return;
      }

      JaxWsProxyFactoryBean fact = new JaxWsProxyFactoryBean();
      fact.setAddress("udp://:" + PORT + "/foo");
      Greeter g = fact.create(Greeter.class);
      Assert.assertEquals("Hello World", g.greetMe("World"));
      ((java.io.Closeable) g).close();
   }

   @Test
   public void testLargeRequest() throws Exception
   {
      JaxWsProxyFactoryBean fact = new JaxWsProxyFactoryBean();
      fact.setAddress("udp://localhost:" + PORT);
      Greeter g = fact.create(Greeter.class);
      StringBuilder b = new StringBuilder(100000);
      for (int x = 0; x < 10000; x++)
      {
         b.append("Hello ");
      }
      Assert.assertEquals("Hello " + b.toString(), g.greetMe(b.toString()));

      ((java.io.Closeable) g).close();
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
