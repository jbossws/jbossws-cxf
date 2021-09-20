/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
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
