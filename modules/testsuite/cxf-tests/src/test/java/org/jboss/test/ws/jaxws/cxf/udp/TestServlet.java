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
package org.jboss.test.ws.jaxws.cxf.udp;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;

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
      String hostName = System.getProperty("jboss.bind.address", "localhost");
      if (hostName.startsWith(":"))
      {
         hostName = "[" + hostName + "]";
      }
      return hostName;
   }
}
