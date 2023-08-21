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
package org.jboss.test.ws.jaxws.cxf.jbws3713;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
