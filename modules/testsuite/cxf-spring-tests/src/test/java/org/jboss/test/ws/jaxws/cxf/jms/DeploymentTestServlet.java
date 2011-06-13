/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.jms;

import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;

@WebServlet(name = "TestServlet", urlPatterns = "/*")
public class DeploymentTestServlet extends HttpServlet
{
   private static final long serialVersionUID = 1L;

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      try
      {
         boolean result;
         URL wsdlUrl = Thread.currentThread().getContextClassLoader().getResource("META-INF/wsdl/HelloWorldService.wsdl");
         //start a new bus to avoid affecting the one that could already be assigned to this thread
         Bus bus = BusFactory.newInstance().createBus();
         BusFactory.setThreadDefaultBus(bus);
         try
         {
            QName serviceName = new QName("http://org.jboss.ws/jaxws/cxf/jms", "HelloWorldService");
            Service service = Service.create(wsdlUrl, serviceName);
            
            //JMS test
            HelloWorld proxy = (HelloWorld) service.getPort(new QName("http://org.jboss.ws/jaxws/cxf/jms", "HelloWorldImplPort"), HelloWorld.class);
            result = "Hi".equals(proxy.echo("Hi"));
         }
         finally
         {
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
}
