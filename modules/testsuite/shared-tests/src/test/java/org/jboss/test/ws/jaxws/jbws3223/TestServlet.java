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
package org.jboss.test.ws.jaxws.jbws3223;

import java.io.IOException;
import java.io.PrintWriter;

import java.net.URL;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
      URL wsdlURL = new URL("http://" + getHost() + ":" + req.getLocalPort() + "/jaxws-jbws3223?wsdl");
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
