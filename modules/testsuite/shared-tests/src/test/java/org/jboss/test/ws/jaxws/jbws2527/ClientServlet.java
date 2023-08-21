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
package org.jboss.test.ws.jaxws.jbws2527;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.ws.WebServiceRef;

import org.jboss.logging.Logger;

/**
 * Client servlet using the injected service
 *
 * @author richard.opalka@jboss.com
 */
public class ClientServlet extends HttpServlet
{

   private static final long serialVersionUID = -2150764371358503995L;

   private final Logger log = Logger.getLogger(ClientServlet.class);

   @WebServiceRef(name="service/jbws2527service")
   HelloService service;

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      boolean result = false;
      try
      {
         log.info("service = "+service);
         Hello hello = service.getHelloPort();
         result = hello.getMessageContextTest();
         log.info("result = " + result);
      }
      catch (Exception e)
      {
         log.error("Error while invoking service!", e);
         throw new ServletException(e);
      }
      resp.getWriter().print(result);
   }

}
