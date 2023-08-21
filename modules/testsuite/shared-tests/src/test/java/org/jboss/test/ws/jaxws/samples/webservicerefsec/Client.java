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
package org.jboss.test.ws.jaxws.samples.webservicerefsec;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.WebServiceRef;

import org.jboss.logging.Logger;

public class Client extends HttpServlet
{
   private static final long serialVersionUID = -5930858947901509123L;

   // Provide logging
   private static Logger log = Logger.getLogger(Client.class);

   @WebServiceRef
   EndpointService authorizedService;

   @WebServiceRef
   EndpointService unauthorizedService;

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      String inStr = req.getParameter("echo");
      log.info("doGet: " + inStr);

      String outStr = null;
      Endpoint ep = authorizedService.getPort(Endpoint.class);
      log.info("Performing invocation with username: " + ((BindingProvider) ep).getRequestContext().get(BindingProvider.USERNAME_PROPERTY));
      outStr = ep.echo(inStr);
      if (inStr.equals(outStr) == false)
         throw new WebServiceException("Invalid echo return: " + inStr);

      boolean invokationSucceeded = false;
      try
      {
         Endpoint uep = unauthorizedService.getPort(Endpoint.class);
         log.info("Performing invocation with username: " + ((BindingProvider) uep).getRequestContext().get(BindingProvider.USERNAME_PROPERTY));
         uep.echo(inStr);
         invokationSucceeded = true;
      }
      catch (Exception e)
      {
         log.info(e);
      }
      if (invokationSucceeded)
      {
         throw new RuntimeException("Expected exception!");
      }

      res.getWriter().print(outStr);
   }
}
