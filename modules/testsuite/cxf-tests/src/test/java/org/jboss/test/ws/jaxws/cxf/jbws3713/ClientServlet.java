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
package org.jboss.test.ws.jaxws.cxf.jbws3713;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "ClientServlet", urlPatterns = "/client/*")
public class ClientServlet extends HttpServlet
{
   private static final long serialVersionUID = 1L;
   private static final Pattern VALID_IPV6_PATTERN;
   private static final String ipv6Pattern = "^([\\dA-F]{1,4}:|((?=.*(::))(?!.*\\3.+\\3))\\3?)([\\dA-F]{1,4}(\\3|:\\b)|\\2){5}(([\\dA-F]{1,4}(\\3|:\\b|$)|\\2){2}|(((2[0-4]|1\\d|[1-9])?\\d|25[0-5])\\.?\\b){4})\\z";
   static
   {
      try
      {
         VALID_IPV6_PATTERN = Pattern.compile(ipv6Pattern, Pattern.CASE_INSENSITIVE);
      }
      catch (PatternSyntaxException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      String strategy = req.getParameter("strategy");
      if (strategy == null || strategy.length() == 0)
         throw new ServletException("strategy not specified!");
      String path = req.getParameter("path");
      if (path == null || path.length() == 0)
         throw new ServletException("path not specified!");
      String threads = req.getParameter("threads");
      if (threads == null || threads.length() == 0)
         throw new ServletException("threads not specified!");
      String calls = req.getParameter("calls");
      if (calls == null || calls.length() == 0)
         throw new ServletException("calls not specified!");
      
      PrintWriter w = res.getWriter();
      final URL wsdlURL = new URL("http://" + toIPv6URLFormat(req.getLocalAddr()) + ":" + req.getLocalPort() + path + "?wsdl");
      Helper helper = new Helper();
      w.write(helper.run(wsdlURL, strategy, Integer.parseInt(threads), Integer.parseInt(calls)).toString());
   }
   
   private String toIPv6URLFormat(final String host)
   {
      boolean isIPv6URLFormatted = false;
      //strip out IPv6 URL formatting if already provided...
      if (host.startsWith("[") && host.endsWith("]")) {
         isIPv6URLFormatted = true;
      }
      //return IPv6 URL formatted address
      if (isIPv6URLFormatted) {
         return host;
      } else {
         Matcher m = VALID_IPV6_PATTERN.matcher(host);
         return m.matches() ? "[" + host + "]" : host;
      }
   }
}
