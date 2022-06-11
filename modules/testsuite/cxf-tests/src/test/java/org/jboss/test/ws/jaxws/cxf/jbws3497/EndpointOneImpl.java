/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.jbws3497;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.annotation.Resource;
import javax.ejb.Stateless;
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
