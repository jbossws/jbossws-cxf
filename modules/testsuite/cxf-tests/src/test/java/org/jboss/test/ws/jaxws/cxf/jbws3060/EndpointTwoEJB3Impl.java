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
package org.jboss.test.ws.jaxws.cxf.jbws3060;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.ejb.Stateless;
import jakarta.jws.Oneway;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;

import org.jboss.logging.Logger;

@WebService(name = "EndpointTwo", targetNamespace = "http://org.jboss.ws.jaxws.cxf/jbws3060", serviceName = "ServiceTwo")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@Stateless
public class EndpointTwoEJB3Impl
{
   private static AtomicInteger count = new AtomicInteger(0);
   
   @WebMethod
   public String sayHello(String input)
   {
      Logger.getLogger(this.getClass()).info("sayHello: " + input);
      count.incrementAndGet();
      return "Hi " + input;
   }
   
   @WebMethod
   @Oneway
   public void sayHelloOneWay(String input)
   {
      Logger.getLogger(this.getClass()).info("sayHelloOneWay: " + input);
      count.incrementAndGet();
   }
   
   @WebMethod
   public int getCount()
   {
      return count.get();
   }
}
