/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.jbws3060;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.jws.Oneway;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;

import org.jboss.logging.Logger;

@WebService(name = "EndpointTwo", targetNamespace = "http://org.jboss.ws.jaxws.cxf/jbws3060", serviceName = "ServiceTwo")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class EndpointTwoImpl
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
