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

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.annotation.Resource;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import jakarta.xml.ws.WebServiceContext;

import org.apache.cxf.interceptor.InInterceptors;
import org.apache.cxf.message.Message;
import org.jboss.logging.Logger;

@WebService(name = "AnotherEndpoint", targetNamespace = "http://org.jboss.ws.jaxws.cxf/interceptors", serviceName = "AnotherService", portName = "AnotherEndpointPort")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@InInterceptors(interceptors="org.jboss.test.ws.jaxws.cxf.interceptors.DeclaredInterceptor")
public class AnotherEndpointImpl
{
   private static AtomicInteger counter = new AtomicInteger(0);
   
   @Resource
   WebServiceContext ctx;
   
   @WebMethod
   public String echo(String input)
   {
      Logger.getLogger(this.getClass()).info("echo:." + input);
      Message cxfMessage = (Message)ctx.getMessageContext().get(Message.class.getName());
      cxfMessage.getExchange().put(Counter.class, new Counter()
      {
         @Override
         public void increment()
         {
            counter.incrementAndGet();
         }
      });
      return input + "." + cxfMessage.get(StringBuilder.class) + "." + counter.get();
   }
}
