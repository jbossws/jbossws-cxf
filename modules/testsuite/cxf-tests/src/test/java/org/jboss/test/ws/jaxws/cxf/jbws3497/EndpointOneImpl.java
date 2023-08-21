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
package org.jboss.test.ws.jaxws.cxf.jbws3497;

import java.util.concurrent.atomic.AtomicInteger;
import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.jws.Oneway;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import jakarta.xml.ws.WebServiceContext;

import org.apache.cxf.Bus;
import org.apache.cxf.workqueue.AutomaticWorkQueue;
import org.apache.cxf.workqueue.WorkQueueManager;
import org.jboss.logging.Logger;
import org.jboss.wsf.spi.invocation.EndpointAssociation;
import org.jboss.wsf.stack.cxf.configuration.BusHolder;

@WebService(name = "EndpointOne", targetNamespace = "http://org.jboss.ws.jaxws.cxf/jbws3497", serviceName = "ServiceOne")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@Stateless
public class EndpointOneImpl
{
   private static AtomicInteger count = new AtomicInteger(0);
   
   @Resource
   WebServiceContext ctx;
   
   @WebMethod
   public String echo(String input)
   {
      //this is just a verification, so going the dirty way...
      Bus bus = EndpointAssociation.getEndpoint().getService().getDeployment().getAttachment(BusHolder.class).getBus();
      AutomaticWorkQueue queue = bus.getExtension(WorkQueueManager.class).getAutomaticWorkQueue();
      Long qs = null;
      Integer it = null;
      try
      {
         qs = (Long) queue.getClass().getMethod("getMaxSize").invoke(queue);
         it = (Integer) queue.getClass().getMethod("getHighWaterMark").invoke(queue);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      if (qs != 10)
      {
         throw new RuntimeException("Expected max queue size '10' but got '" + qs + "'!");
      }
      if (it != 8)
      {
         throw new RuntimeException("Expected highWaterMark '8' but got '" + it + "'!");
      }
      Logger.getLogger(this.getClass()).info("echo: " + input);
      count.incrementAndGet();
      return input;
   }
   
   @WebMethod
   @Oneway
   public void echoOneWay(String input)
   {
      Logger.getLogger(this.getClass()).info("echoOneWay: " + input);
      count.incrementAndGet();
   }
   
   @WebMethod
   public int getCount()
   {
      return count.get();
   }
}
