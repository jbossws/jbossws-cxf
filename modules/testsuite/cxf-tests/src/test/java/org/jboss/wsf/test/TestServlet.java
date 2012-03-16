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
package org.jboss.wsf.test;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author alessio.soldano@jboss.com
 * @since 01-Apr-2011
 *
 */
@WebServlet(name = "TestServlet", urlPatterns = "/*")
public class TestServlet extends HttpServlet
{
   private static final long serialVersionUID = 1L;

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      String helperClassName = req.getParameter("helper");
      if (helperClassName == null || helperClassName.length() == 0)
         throw new ServletException("helper not specified!");
      String path = req.getParameter("path");
      if (path == null || path.length() == 0)
         throw new ServletException("path not specified!");
      try
      {
         ClientHelper helper = (ClientHelper) Class.forName(helperClassName).newInstance();
         String hostName = System.getProperty("jboss.bind.address", "localhost");
         if (hostName.startsWith(":"))
         {
            hostName = "[" + hostName + "]";
         }
         helper.setTargetEndpoint("http://" + hostName + ":8080" + path);
         List<String> failedTests = new LinkedList<String>();
         List<String> errorTests = new LinkedList<String>();
         Method[] methods = helper.getClass().getMethods();
         String methodName = req.getParameter("method");
         int testsRun = 0;
         if (methodName != null && methodName.length() > 0)
         {
            Method m = null;
            m = helper.getClass().getMethod(methodName);
            testsRun++;
            invokeMethod(m, helper, failedTests, errorTests);
         }
         else
         {
            for (Method m : methods)
            {
               if (m.getName().startsWith("test") && m.getParameterTypes().length == 0
                     && m.getReturnType().equals(boolean.class))
               {
                  testsRun++;
                  invokeMethod(m, helper, failedTests, errorTests);
               }
            }
         }
         if (failedTests.isEmpty() && errorTests.isEmpty())
         {
            res.getWriter().print(testsRun);
         }
         else
         {
            PrintWriter w = res.getWriter();
            w.print("# Failed tests: ");
            for (Iterator<String> it = failedTests.iterator(); it.hasNext();)
            {
               w.print(it.next());
               if (it.hasNext())
                  w.print(", ");
            }
            res.getWriter().print(" # Error tests: ");
            for (Iterator<String> it = errorTests.iterator(); it.hasNext();)
            {
               w.print(it.next());
               if (it.hasNext())
                  w.print(", ");
            }
         }
      }
      catch (Exception e)
      {
         throw new ServletException(e);
      }
   }
   
   private void invokeMethod(Method m, ClientHelper helper, List<String> failedTests, List<String> errorTests) throws ServletException
   {
      try
      {
         if (!(Boolean)m.invoke(helper))
         {
            failedTests.add(m.getName());
         }
      }
      catch (InvocationTargetException e)
      {
         Throwable thrownException = e.getTargetException();
         errorTests.add(m.getName() + ": " + thrownException.getClass().getName() + " " + thrownException.getMessage());
         thrownException.printStackTrace();
      }
      catch (Exception e)
      {
         throw new ServletException(e);
      }
   }
}
