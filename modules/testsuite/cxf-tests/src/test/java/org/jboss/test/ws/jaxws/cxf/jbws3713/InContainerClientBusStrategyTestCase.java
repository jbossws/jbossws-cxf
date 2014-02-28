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
package org.jboss.test.ws.jaxws.cxf.jbws3713;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.stack.cxf.client.Constants;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

public class InContainerClientBusStrategyTestCase extends JBossWSTest
{
   public final String endpointAddress = "http://" + getServerHost() + ":8080/jaxws-cxf-jbws3713/HelloService";

   public static Test suite()
   {
      return new JBossWSTestSetup(InContainerClientBusStrategyTestCase.class, "jaxws-cxf-jbws3713.war,jaxws-cxf-jbws3713-client.war");
   }

   public void testEndpoint() throws Exception
   {
      HelloWs port = getPort(endpointAddress);
      HelloRequest request = new HelloRequest();
      request.setInput("hello");
      HelloResponse response = port.doHello(request);
      assertEquals(2, response.getMultiHello().size());
      assertTrue(response.getMultiHello().contains("hello"));
      assertTrue(response.getMultiHello().contains("world"));
   }
   
   public void testClientWithNewBusStrategy() throws Exception
   {
      final int threadPoolSize = 10;
      final int invocations = 50;
      int busCount = callServlet("client-using-threadlocal", Constants.NEW_BUS_STRATEGY, threadPoolSize, invocations);
      assertEquals(threadPoolSize, busCount);
      
      busCount = callServlet("client", Constants.NEW_BUS_STRATEGY, threadPoolSize, invocations);
      assertEquals(invocations, busCount);
   }
   
   public void testClientWithThreadBusStrategy() throws Exception
   {
      final int threadPoolSize = 10;
      final int invocations = 50;
      int busCount = callServlet("client-using-threadlocal", Constants.THREAD_BUS_STRATEGY, threadPoolSize, invocations);
      assertEquals(threadPoolSize, busCount);
      
      busCount = callServlet("client", Constants.THREAD_BUS_STRATEGY, threadPoolSize, invocations);
      assertEquals(threadPoolSize, busCount);
   }
   
   public void testClientWithTCCLBusStrategy() throws Exception
   {
      final int threadPoolSize = 10;
      final int invocations = 50;
      int busCount = callServlet("client-using-threadlocal", Constants.TCCL_BUS_STRATEGY, threadPoolSize, invocations);
      assertEquals(1, busCount);
      
      busCount = callServlet("client", Constants.TCCL_BUS_STRATEGY, threadPoolSize, invocations);
      assertEquals(1, busCount);
   }
   
   private static int callServlet(String pattern, String strategy, int threads, int calls) throws Exception {
      URL url = new URL("http://" + getServerHost() + ":8080/jaxws-cxf-jbws3713-client/" + pattern + "?strategy="
            + strategy + "&host=" + getServerHost() + "&threads=" + threads + "&calls=" + calls);
      return Integer.parseInt(IOUtils.readAndCloseStream(url.openStream()));
   }

   private HelloWs getPort(String publishURL) throws Exception
   {
      URL wsdlURL = new URL(publishURL + "?wsdl");
      QName qname = new QName("http://hello/test", "HelloService");
      Service service = Service.create(wsdlURL, qname);
      return service.getPort(HelloWs.class);
   }
}
