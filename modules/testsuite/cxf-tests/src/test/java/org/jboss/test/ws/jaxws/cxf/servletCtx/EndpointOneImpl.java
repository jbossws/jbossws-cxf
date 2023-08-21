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
