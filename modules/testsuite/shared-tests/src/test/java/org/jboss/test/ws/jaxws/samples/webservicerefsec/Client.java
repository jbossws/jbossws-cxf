/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.webservicerefsec;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceRef;

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
