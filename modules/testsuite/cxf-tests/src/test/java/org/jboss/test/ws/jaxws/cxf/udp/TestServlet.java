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
package org.jboss.test.ws.jaxws.cxf.udp;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import jakarta.xml.ws.Endpoint;
import jakarta.xml.ws.Service;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.jboss.ws.common.utils.AddressUtils;

@WebServlet(name = "TestServlet", urlPatterns = "/*")
public class TestServlet extends HttpServlet
{
   private static final long serialVersionUID = 1L;

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      try
      {
         boolean result = false;
         //start a new bus to avoid affecting the one that could already be assigned to this thread
         Bus bus = BusFactory.newInstance().createBus();
         BusFactory.setThreadDefaultBus(bus);
         Object implementor = new HelloWorldImpl();
         final String address = "soap.udp://" + getHost() + ":9435";
         //Endpoint ep = Endpoint.publish("soap.udp://:9435", implementor);
         Endpoint ep = Endpoint.publish(address, implementor);
         try
         {
            final QName serviceName = new QName("http://org.jboss.ws/jaxws/cxf/udp", "HelloWorldService");
            final QName udpPortName = new QName("http://org.jboss.ws/jaxws/cxf/udp", "UDPHelloWorldImplPort");
            Service service = Service.create(serviceName);
            service.addPort(udpPortName, "http://schemas.xmlsoap.org/soap/", address);
            HelloWorld proxy = (HelloWorld) service.getPort(udpPortName, HelloWorld.class);
            result = "Hi".equals(proxy.echo("Hi"));
         }
         finally
         {
            ep.stop();
            bus.shutdown(true);
         }
         res.getWriter().print(result);
      }
      catch (Throwable t)
      {
         t.printStackTrace();
         throw new ServletException(t);
      }
   }
   
   private static String getHost() {
      return toIPv6URLFormat(System.getProperty("jboss.bind.address", "localhost"));
   }
   
   private static String toIPv6URLFormat(final String host)
   {
      boolean isIPv6URLFormatted = false;
      if (host.startsWith("[") && host.endsWith("]")) {
         isIPv6URLFormatted = true;
      }
      //return IPv6 URL formatted address
      if (isIPv6URLFormatted) {
         return host;
      } else {
         return AddressUtils.isValidIPv6Address(host) ? "[" + host + "]" : host;
      }
   }
}
