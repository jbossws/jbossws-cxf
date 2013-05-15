/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws2307;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.RespectBinding;
import javax.xml.ws.WebServiceRef;
import javax.xml.ws.soap.Addressing;
import javax.xml.ws.soap.MTOM;
import javax.xml.ws.soap.SOAPBinding;

import org.jboss.logging.Logger;

/**
 * Client servlet using the injected service
 * 
 * @author alessio.soldano@jboss.com
 */
@MTOM
@RespectBinding
@Addressing
@WebServiceRef(name="service/jbws2307service/asdf", type=HelloService.class)
public class ClientServlet extends HttpServlet
{
   private Logger log = Logger.getLogger(ClientServlet.class);
   
   @WebServiceRef(name="service/jbws2307service")
   HelloService service;
   
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      boolean result = false;
      try
      {
         log.info("service = "+service);
         Hello hello = service.getHelloPort();
         SOAPBinding binding = (SOAPBinding)((BindingProvider)hello).getBinding();
         if ("true".equals(req.getParameter("mtom")))
         {
            log.info("mtom enabled = " + binding.isMTOMEnabled());
            if (!binding.isMTOMEnabled())
            {
           	 throw new Exception("Expected mtom enabled because of port-component configuration in the DD");
            }
         }
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
