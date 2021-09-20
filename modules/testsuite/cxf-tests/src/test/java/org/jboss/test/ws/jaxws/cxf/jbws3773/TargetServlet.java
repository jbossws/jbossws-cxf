/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.jbws3773;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet(name = "TestServlet", urlPatterns = "/target/*")
public class TargetServlet extends HttpServlet
{
   private final StringBuffer result = new StringBuffer();

   protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      if (result.length() > 0 && req.getRequestURI().endsWith("result"))
      {
         PrintWriter pw = new PrintWriter(res.getWriter());
         pw.write(result.toString());
         pw.close();
      }
      //clear result
      result.setLength(0);
   }

   protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      byte[] buffer = null;
      int length = req.getContentLength();
      if (length > 0)
      {
         buffer = new byte[length];
         req.getInputStream().read(buffer);
         
      }
      if (req.getRequestURI().endsWith("replyTo"))
      {
         result.append("ReplyTo:");
         result.append(new String(buffer));
      }

   }

}