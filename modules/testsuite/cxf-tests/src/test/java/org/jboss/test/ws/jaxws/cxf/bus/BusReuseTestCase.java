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

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;

import junit.framework.Test;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.stack.cxf.client.UseNewBusFeature;
import org.jboss.wsf.stack.cxf.client.UseThreadBusFeature;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;

/**
 * A testcase for verifying proper behaviour of the UseNewBusFeature on
 * JAXWS Service creation.
 * 
 * @author alessio.soldano@jboss.com
 * @since 28-Aug-2013
 *
 */
public class BusReuseTestCase extends JBossWSTest
{
   public final String WSDL_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-cxf-bus-wsdl";
   
   public static Test suite()
   {
      return new JBossWSCXFTestSetup(BusReuseTestCase.class, DeploymentArchives.SERVER + ", " + DeploymentArchives.SERVER_2);
   }
   
   public void testReuse() throws Exception
   {
      //odd wsdl GETs return WSDL doc with invalid soap:address
      //even wsdl GETs return WSDL doc with valid soap:address
      final String wsdl1 = readWsdl(WSDL_ADDRESS); //invalid
      final String wsdl2 = readWsdl(WSDL_ADDRESS); //valid
      final String wsdl3 = readWsdl(WSDL_ADDRESS); //invalid
      final String wsdl4 = readWsdl(WSDL_ADDRESS); //valid
      assertEquals(wsdl1, wsdl3);
      assertEquals(wsdl2, wsdl4);
      assertFalse(wsdl1.equals(wsdl2));
      
      Bus bus = BusFactory.newInstance().createBus();
      try {
         BusFactory.setThreadDefaultBus(bus);
         Endpoint port = getPort(WSDL_ADDRESS, bus, new UseThreadBusFeature()); //invalid
         try {
            performInvocation(port);
            fail("Failure expected, as the wsdl soap:address is not valid!");
         } catch (WebServiceException wse) {
            assertTrue(wse.getCause().getMessage().contains("InvalidEndpoint"));
         }
         
         port = getPort(WSDL_ADDRESS, bus, new UseThreadBusFeature()); //valid
         try {
            performInvocation(port);
            fail("Failure expected, as the WSDLManager for the bus will return the invalid wsdl");
         } catch (WebServiceException wse) {
            assertTrue(wse.getCause().getMessage().contains("InvalidEndpoint"));
         }
         
         port = getPort(WSDL_ADDRESS, bus, new UseThreadBusFeature()); //invalid
         
         port = getPort(WSDL_ADDRESS, bus, new UseNewBusFeature()); //valid
         //the port should now actually be built against the valid wsdl
         //as a new bus should have been started (with a new WSDLManager)
         //so the invocation is expected to succeed
         performInvocation(port);
         
         port = getPort(WSDL_ADDRESS, bus, new UseThreadBusFeature()); //invalid
         
         port = getPort(WSDL_ADDRESS, bus, new UseNewBusFeature(false)); //valid
         try {
            performInvocation(port);
            fail("Failure expected, as the WSDLManager for the bus will return the invalid wsdl (disabled feature used)");
         } catch (WebServiceException wse) {
            assertTrue(wse.getCause().getMessage().contains("InvalidEndpoint"));
         }
      } finally {
         bus.shutdown(true);
      }
   }
   
   private String readWsdl(String addr) throws Exception {
      return IOUtils.readAndCloseStream(new URL(addr).openStream());
   }
   
   protected static void performInvocation(Endpoint endpoint)
   {
      String result = endpoint.echo("Alessio");
      assert ("Alessio".equals(result));
   }
   
   protected static Endpoint getPort(String wsdlAddr, Bus currentThreadBus, WebServiceFeature... features) throws MalformedURLException
   {
      QName serviceName = new QName("http://org.jboss.ws/bus", "EndpointService");
      Service service = Service.create(new URL(wsdlAddr), serviceName, features);
      //check the current thread bus has not changed (even if we used the UseNewBusFeature)
      assertEquals(currentThreadBus, BusFactory.getThreadDefaultBus(false));
      QName portQName = new QName("http://org.jboss.ws/bus", "EndpointPort");
      return (Endpoint) service.getPort(portQName, Endpoint.class);
   }
}
