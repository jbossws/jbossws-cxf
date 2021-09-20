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
