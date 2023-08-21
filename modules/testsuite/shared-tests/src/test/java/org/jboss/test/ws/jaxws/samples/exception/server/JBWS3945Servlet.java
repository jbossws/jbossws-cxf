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
package org.jboss.test.ws.jaxws.samples.exception.server;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JBWS3945Servlet extends HttpServlet
{
   private static final long serialVersionUID = 1L;

   public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      res.setStatus(400);
      res.setContentType("application/soap+xml");
      res.getOutputStream().println("<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\"><soap:Body>" +
      		"<soap:Fault><soap:Code><soap:Value>soap:Sender</soap:Value><soap:Subcode><soap:Value xmlns:ns1=\"http://ws.gss.redhat.com/\">ns1:AnException</soap:Value></soap:Subcode>" +
      		"</soap:Code><soap:Reason><soap:Text xml:lang=\"it\">this is a fault string!</soap:Text></soap:Reason><soap:Role>mr.actor</soap:Role><soap:Detail><test/></soap:Detail>" +
      				"</soap:Fault></soap:Body></soap:Envelope>");
   }
}
