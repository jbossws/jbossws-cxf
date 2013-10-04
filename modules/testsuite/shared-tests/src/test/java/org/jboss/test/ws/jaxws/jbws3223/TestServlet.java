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
package org.jboss.test.ws.jaxws.jbws3223;

import java.io.IOException;
import java.io.PrintWriter;

import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.ws.common.utils.AddressUtils;

@SuppressWarnings("serial")
public class TestServlet extends HttpServlet
{
   protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      // Invoke the endpoint
      String param = req.getParameter("param");
      boolean clCheck = Boolean.parseBoolean(req.getParameter("clCheck"));
      Client client = new Client(clCheck);
      URL wsdlURL = new URL("http://" + getHost() + ":8080/jaxws-jbws3223?wsdl");
      String retStr = client.run(param, wsdlURL);
      
      // Return the result
      PrintWriter pw = new PrintWriter(res.getWriter());
      pw.print(retStr);
      pw.close();
   }
   
   private static String getHost() {
      return toIPv6URLFormat(System.getProperty("jboss.bind.address", "localhost"));
   }
   
   private static String toIPv6URLFormat(final String host)
   {
      boolean isIPv6URLFormatted = false;
      if (host.startsWith("[") && host.endsWith("]")) {
         isIPv6URLFormatted = true;
      }
      //return IPv6 URL formatted address
      if (isIPv6URLFormatted) {
         return host;
      } else {
         return AddressUtils.isValidIPv6Address(host) ? "[" + host + "]" : host;
      }
   }
   
}
