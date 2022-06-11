/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.servletCtx;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.annotation.Resource;
import jakarta.jws.Oneway;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.xml.ws.WebServiceContext;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.soap.Addressing;

import org.jboss.logging.Logger;

@WebService(name = "EndpointOne", targetNamespace = "http://org.jboss.ws.jaxws.cxf/servletCtx", serviceName = "ServiceOne")
@Addressing
public class EndpointOneImpl implements EndpointOne
{
   private static AtomicInteger count1 = new AtomicInteger(0);
   private static AtomicInteger count2 = new AtomicInteger(0);
   @Resource
   private WebServiceContext context;

   @WebMethod
   public String echo(String input)
   {
      count1.incrementAndGet();
      MessageContext msgContext = context.getMessageContext();
      HttpServletRequest request = (HttpServletRequest)msgContext.get(MessageContext.SERVLET_REQUEST);
      final String scheme = request.getScheme();
      Logger.getLogger(this.getClass()).info("echo: " + input + ", scheme: " + scheme);
      if (scheme != null) {
         count2.incrementAndGet();
      }
      return input;
   }

   @WebMethod
   @Oneway
   public void echoOneWay(String input)
   {
      count1.incrementAndGet();
      MessageContext msgContext = context.getMessageContext();
      HttpServletRequest request = (HttpServletRequest)msgContext.get(MessageContext.SERVLET_REQUEST);
      final String scheme = request.getScheme();
      Logger.getLogger(this.getClass()).info("echoOneWay: " + input + ", scheme: " + scheme);
      if (scheme != null) {
         count2.incrementAndGet();
      }
   }

   @WebMethod
   public int getCount1()
   {
      return count1.get();
   }
   
   @WebMethod
   public int getCount2()
   {
      return count2.get();
   }
}
