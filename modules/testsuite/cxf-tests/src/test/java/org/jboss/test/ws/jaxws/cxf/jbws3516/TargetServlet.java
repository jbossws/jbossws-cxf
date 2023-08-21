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
package org.jboss.test.ws.jaxws.cxf.jbws3516;

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
   public StringBuffer result = new StringBuffer();

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
      if (req.getRequestURI().endsWith("faultTo"))
      {
         result.append("FaultTo:");
         result.append(new String(buffer));
         
      }
      if (req.getRequestURI().endsWith("replyTo"))
      {
         result.append("ReplyTo:");
         result.append(new String(buffer));
      }

   }

}