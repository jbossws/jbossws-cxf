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
package org.jboss.test.ws.jaxws.cxf.jms_http;

import java.io.IOException;
import java.net.URL;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

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
            Service service = Service.create(wsdlUrl, new QName("http://org.jboss.ws/jaxws/cxf/jms", "HelloWorldService"));
            //HelloWorldServiceLocal service references local connection factory (for in-VM use)
            Service serviceLocal = Service.create(wsdlUrl, new QName("http://org.jboss.ws/jaxws/cxf/jms", "HelloWorldServiceLocal"));
            
            //JMS test
            HelloWorld proxy = (HelloWorld) serviceLocal.getPort(new QName("http://org.jboss.ws/jaxws/cxf/jms", "HelloWorldImplPort"), HelloWorld.class);
            result = "Hi".equals(proxy.echo("Hi"));
            //HTTP test
            HelloWorld httpProxy = (HelloWorld) service.getPort(new QName("http://org.jboss.ws/jaxws/cxf/jms", "HttpHelloWorldImplPort"), HelloWorld.class);
            result = result && "(http) Hi".equals(httpProxy.echo("Hi"));
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
