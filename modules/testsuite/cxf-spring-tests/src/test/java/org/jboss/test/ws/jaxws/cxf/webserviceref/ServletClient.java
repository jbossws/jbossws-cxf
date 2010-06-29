/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.webserviceref;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceRef;

import org.jboss.logging.Logger;

/**
 * 
 * @author alessio.soldano@jboss.com
 * @since 19-Nov-2009
 *
 */
public class ServletClient extends HttpServlet
{
   private static final long serialVersionUID = 1L;

   // Provide logging
   private static Logger log = Logger.getLogger(ServletClient.class);

   @WebServiceRef(value = EndpointService.class, type = Endpoint.class, wsdlLocation = "WEB-INF/wsdl/Endpoint.wsdl")
   public Endpoint port;

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      String inStr = req.getParameter("echo");
      log.info("doGet: " + inStr);

      String outStr = port.echo(inStr);

      String expectedResult = inStr + ":out:Handler:in:Handler";
      if (expectedResult.equals(outStr) == false)
         throw new WebServiceException("Invalid echo return; expected '" + expectedResult + "' but got '" + outStr + "'");

      res.getWriter().print(inStr);
   }
}
