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
package org.jboss.test.ws.jaxws.cxf.interceptors;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.annotation.Resource;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import jakarta.xml.ws.WebServiceContext;
import jakarta.xml.ws.WebServiceException;

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
