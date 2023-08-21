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
package org.jboss.test.helper;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
      String helperClassName = req.getParameter("helper");
      if (helperClassName == null || helperClassName.length() == 0)
         throw new ServletException("helper not specified!");
      String path = req.getParameter("path");
      if (path == null || path.length() == 0)
         throw new ServletException("path not specified!");
      try
      {
         ClientHelper helper = (ClientHelper) Class.forName(helperClassName).getDeclaredConstructor().newInstance();
         String jbossBindAddress = toIPv6URLFormat(System.getProperty("jboss.bind.address", "localhost"));
         helper.setTargetEndpoint("http://" + jbossBindAddress + ":" + req.getLocalPort() + path);
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
   
   private void invokeMethod(Method m, ClientHelper helper, List<String> failedTests, List<String> errorTests) throws ServletException
   {
      try
      {
         Object obj = m.invoke(helper);
         if (obj != null && !(Boolean)obj)
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
