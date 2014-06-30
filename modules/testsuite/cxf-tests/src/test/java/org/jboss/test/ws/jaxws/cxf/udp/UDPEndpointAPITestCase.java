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

import java.io.File;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;

/**
 * Test case for publishing a UDP (SOAP-over-UDP 1.1) endpoint through API
 *
 * @author alessio.soldano@jboss.com
 * @since 6-May-2013
 */
public final class UDPEndpointAPITestCase extends JBossWSTest
{
   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.JarDeployment("jaxws-cxf-udp-api-client.jar") { {
         archive
               .addManifest()
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/udp/META-INF/wsdl/HelloWorldService.wsdl"), "wsdl/HelloWorldService.wsdl");
         }
      });
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-cxf-udp-api.war") { {
         archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: org.jboss.ws.common,org.jboss.ws.cxf.jbossws-cxf-client services\n"))
               .addClass(org.jboss.test.ws.jaxws.cxf.udp.HelloWorld.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.udp.HelloWorldImpl.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.udp.TestServlet.class)
               .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/udp/META-INF/wsdl/HelloWorldService.wsdl"), "META-INF/wsdl/HelloWorldService.wsdl");
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSCXFTestSetup(UDPEndpointAPITestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
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
