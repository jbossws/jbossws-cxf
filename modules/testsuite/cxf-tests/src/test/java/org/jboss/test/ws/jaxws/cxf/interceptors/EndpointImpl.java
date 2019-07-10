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
package org.jboss.test.ws.jaxws.cxf.interceptors;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;

import org.apache.cxf.interceptor.InInterceptors;
import org.apache.cxf.message.Message;
import org.jboss.logging.Logger;

@WebService(name = "MyEndpoint", targetNamespace = "http://org.jboss.ws.jaxws.cxf/interceptors", serviceName = "MyService", portName = "MyEndpointPort")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@InInterceptors(interceptors="org.jboss.test.ws.jaxws.cxf.interceptors.DeclaredInterceptor")
public class EndpointImpl
{
   private static AtomicInteger counter = new AtomicInteger(0);
   
   @Resource
   WebServiceContext ctx;
   
   @WebMethod
   public String echo(String input)
   {
      Logger.getLogger(this.getClass()).info("echo: " + input);
      Message cxfMessage = (Message)ctx.getMessageContext().get(Message.class.getName());
      cxfMessage.getExchange().put(Counter.class, new Counter()
      {
         @Override
         public void increment()
         {
            counter.incrementAndGet();
         }
      });
      return input + " " + cxfMessage.get(StringBuilder.class) + " " + counter.get();
   }
   @WebMethod
   public String echoException(String input) throws WebServiceException {
      throw new WebServiceException("Intended Exception");
   }
   
   public String getException()
   {

      StringBuffer buffer = new StringBuffer();
      if (!JBossWSFaultListener.exceptions.isEmpty())
      {
         JBossWSFaultListener.exceptions.forEach((k, v) -> {
            buffer.append(k);
            buffer.append(":");
            buffer.append(v);
            buffer.append(";");
         });
      }
      return buffer.toString();
   }
}
