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
package org.jboss.wsf.stack.cxf.addons.transports.udp;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

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
