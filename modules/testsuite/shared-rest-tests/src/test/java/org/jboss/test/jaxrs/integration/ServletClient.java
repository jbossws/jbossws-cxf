/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.jaxrs.integration;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.ext.RuntimeDelegate;

/**
 * 
 * 
 * @author alessio.soldano@jboss.com
 * @since 28-Jan-2016
 *
 */
@WebServlet(urlPatterns="/test")
public class ServletClient extends HttpServlet
{
   private static final long serialVersionUID = 1L;
   private static final String EXP_CLIENT_BUILDER = "org.jboss.wsf.stack.cxf.client.ClientBuilderImpl";
   private static final String EXP_RUNTIME_DELEGATE = "org.jboss.wsf.stack.cxf.client.RuntimeDelegateImpl";

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      String inStr = req.getParameter("method");
      try
      {
         if (inStr.equalsIgnoreCase("testClientBuilderIntegration"))
         {
            testClientBuilderIntegration();
         }
         else if (inStr.equalsIgnoreCase("testRuntimeDelegateIntegration"))
         {
            testRuntimeDelegateIntegration();
         }
         else
         {
            throw new IllegalArgumentException("Unsupported test method: " + inStr);
         }
         res.getWriter().print("OK " + inStr);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new IOException(e);
      }
   }
   
   
   private void testClientBuilderIntegration() throws Exception {
      ClientBuilder b = ClientBuilder.newBuilder();
      if (!EXP_CLIENT_BUILDER.equals(b.getClass().getName())) {
         throw new Exception("Expected " + EXP_CLIENT_BUILDER + " but got " + b.getClass().getName());
      }
   }
   
   private void testRuntimeDelegateIntegration() throws Exception {
      RuntimeDelegate b = RuntimeDelegate.getInstance();
      if (!EXP_RUNTIME_DELEGATE.equals(b.getClass().getName())) {
         throw new Exception("Expected " + EXP_RUNTIME_DELEGATE + " but got " + b.getClass().getName());
      }
   }
}
