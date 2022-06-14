/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.mixtype;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.ejb.Stateless;
import jakarta.jws.Oneway;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;

import org.jboss.logging.Logger;

@WebService(targetNamespace = "http://org.jboss.ws.jaxws.cxf/mixtype", serviceName = "EJBServiceOne", portName ="EJBEndpointOnePort", endpointInterface = "org.jboss.test.ws.jaxws.cxf.mixtype.EndpointOne")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@Stateless
public class EndpointOneEJB3Impl implements EndpointOne
{
   private static AtomicInteger count = new AtomicInteger(0);
   
   @WebMethod
   public String echo(String input)
   {
      Logger.getLogger(this.getClass()).info("echo: " + input);
      count.addAndGet(5);
      return input;
   }
   
   @WebMethod
   @Oneway
   public void echoOneWay(String input)
   {
      Logger.getLogger(this.getClass()).info("echoOneWay: " + input);
      count.addAndGet(5);
   }
   
   @WebMethod
   public int getCount()
   {
      return count.get();
   }
}

