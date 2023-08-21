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
package org.jboss.test.ws.jaxws.cxf.bus;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.ws.WebServiceRef;

/**
 * This class verifies the default bus is not changed by
 * basic client use (creation of bus through BusFactory.newInstance().createBus(),
 * SAAJ invocation, endpoint invocation, endpoint invocation using webserviceref).
 * 
 * @author alessio.soldano@jboss.com
 * @since 01-Mar-2011
 *
 */
public class ServletClient extends HttpServlet
{
   private static final long serialVersionUID = 1L;

   @WebServiceRef(value = EndpointService.class, type = Endpoint.class, wsdlLocation = "WEB-INF/wsdl/Endpoint.wsdl")
   public Endpoint port;
   
   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      String inStr = req.getParameter("method");
      try
      {
         if (inStr.equalsIgnoreCase("testBusCreation"))
         {
            testBusCreation();
         }
         else if (inStr.equalsIgnoreCase("testSOAPConnection"))
         {
            testSOAPConnection(req.getParameter("host"));
         }
         else if (inStr.equalsIgnoreCase("testWebServiceRef"))
         {
            testWebServiceRef();
         }
         else if (inStr.equalsIgnoreCase("testWebServiceClient"))
         {
            testWebServiceClient(req.getParameter("host"));
         }
         else
         {
            throw new IllegalArgumentException("Unsupported test method: " + inStr);
         }
         res.getWriter().print("OK " + inStr);
      }
      catch (BusTestException bte)
      {
         res.getWriter().print(bte.getMessage());
      }
      catch (Exception e)
      {
         throw new IOException(e);
      }
   }

   public void testBusCreation() throws BusTestException
   {
      AbstractClient.testBusCreation();
   }

   public void testSOAPConnection(String host) throws BusTestException, Exception
   {
      AbstractClient.testSOAPConnection(host);
   }

   public void testWebServiceRef() throws BusTestException
   {
      AbstractClient.testWebServiceRef(port);
   }

   public void testWebServiceClient(String host) throws BusTestException, Exception
   {
      AbstractClient.testWebServiceClient(host);
   }
}
