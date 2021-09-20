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
package org.jboss.test.ws.jaxws.cxf.bus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * A servlet serving two WSDL documents alternatively
 * 
 * @author alessio.soldano@jboss.com
 * @since 28-Aug-2013
 *
 */
@WebServlet(name = "WSDLServlet", urlPatterns = "/*")
public class WSDLServlet extends HttpServlet
{
   private static final long serialVersionUID = 1L;
   
   private static boolean serveValidWsdl = false;

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      final OutputStream out = res.getOutputStream();
      final InputStream in = Thread.currentThread().getContextClassLoader().getResource(getWsdlName()).openStream();
      byte[] buffer = new byte[1024];
      int length;
      while ((length = in.read(buffer)) > 0)
      {
         out.write(buffer, 0, length);
      }
      in.close();
      out.flush();
   }
   
   private static synchronized String getWsdlName() {
      String result = "ValidAddressEndpoint.wsdl";
      if (!serveValidWsdl) {
         result = "InvalidAddressEndpoint.wsdl";
      }
      serveValidWsdl = !serveValidWsdl;
      return result;
   }

}
