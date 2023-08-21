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
package org.jboss.test.ws.jaxws.samples.webserviceref;

import java.io.IOException;
import java.util.ArrayList;

import javax.naming.InitialContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.WebServiceRef;
import jakarta.xml.ws.WebServiceRefs;
import jakarta.xml.ws.soap.SOAPFaultException;

import org.jboss.logging.Logger;

// Test on type with wsdlLocation
@WebServiceRef(name = "service1", value = EndpointService.class, type = EndpointService.class, wsdlLocation = "WEB-INF/wsdl/Endpoint.wsdl")

// Test multiple on type
@WebServiceRefs
(
   {
      @WebServiceRef(name = "service2", value = EndpointService.class, type = EndpointService.class),
      @WebServiceRef(name = "port1", value = EndpointService.class, type = Endpoint.class)
   }
)
public class ServletClient extends HttpServlet
{

   private static final long serialVersionUID = -3990736104626758280L;

   // Provide logging
   private static Logger log = Logger.getLogger(ServletClient.class);

   // Test on field with name
   @WebServiceRef(name = "EndpointService3")
   public EndpointService service3;

   // Test on field without name
   @WebServiceRef
   public EndpointService service4;

   // Test on method with value
   @WebServiceRef(name = "EndpointService5")
   public void setService5(EndpointService service)
   {
      this.service5 = service;
   }
   private EndpointService service5;

   // Test on method without name
   @WebServiceRef
   public void setService6(EndpointService service)
   {
      this.service6 = service;
   }
   private EndpointService service6;

   // Test on field with name and value
   @WebServiceRef(name = "Port2", value = EndpointService.class)
   public Endpoint port2;

   // Test on field with value
   @WebServiceRef(value = EndpointService.class)
   public Endpoint port3;
   
   @WebServiceRef(value = MultipleEndpointService.class)
   public Endpoint port7;
   
   @WebServiceRef(value = MultipleEndpointService.class)
   public Endpoint port8;

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      String inStr = req.getParameter("echo");
      log.info("doGet: " + inStr);

      ArrayList<Endpoint> ports = new ArrayList<Endpoint>();
      try
      {
         InitialContext iniCtx = new InitialContext();
         ports.add(((EndpointService)iniCtx.lookup("java:comp/env/service1")).getEndpointPort());
         ports.add(((EndpointService)iniCtx.lookup("java:comp/env/service2")).getEndpointPort());
         ports.add(service3.getPort(Endpoint.class));
         ports.add(((EndpointService)iniCtx.lookup("java:comp/env/EndpointService3")).getEndpointPort());
         ports.add(service4.getPort(Endpoint.class));
         ports.add(((EndpointService)iniCtx.lookup("java:comp/env/" + getClass().getName() + "/service4")).getEndpointPort());
         ports.add(service5.getPort(Endpoint.class));
         ports.add(((EndpointService)iniCtx.lookup("java:comp/env/EndpointService5")).getEndpointPort());
         ports.add(service6.getPort(Endpoint.class));
         ports.add(((EndpointService)iniCtx.lookup("java:comp/env/" + getClass().getName() + "/service6")).getEndpointPort());
         ports.add((Endpoint)iniCtx.lookup("java:comp/env/port1"));
         ports.add(port2);
         ports.add((Endpoint)iniCtx.lookup("java:comp/env/Port2"));
         ports.add(port3);
         ports.add((Endpoint)iniCtx.lookup("java:comp/env/" + getClass().getName() + "/port3"));
         ports.add(port8);
      }
      catch (Exception ex)
      {
         log.error("Cannot add port", ex);
         throw new WebServiceException(ex);
      }

      for (Endpoint port : ports)
      {
         String outStr = port.echo(inStr);
         if (inStr.equals(outStr) == false)
            throw new WebServiceException("Invalid echo return: " + inStr);
      }
      try {
         port7.echo("Foo");
         throw new WebServiceException("Expected exception due to SOAP 1.2 message sent to a SOAP 1.1 endpoint");
      } catch (SOAPFaultException sfe) {
         if (!sfe.getMessage().contains("SOAP 1.2")) {
            throw new WebServiceException("Expected reference to SOAP version mismatch in error message, but got: " + sfe.getMessage());
         }
      }

      res.getWriter().print(inStr);
   }
}
