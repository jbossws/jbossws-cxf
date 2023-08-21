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
