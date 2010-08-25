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
package org.jboss.wsf.stack.cxf.addons.transports.httpserver;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.cxf.Bus;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.cxf.transport.http.AbstractHTTPTransportFactory;

/**
 * A Destination/Transport factory for the JDK6 httpserver
 * 
 * @author alessio.soldano@jboss.com
 * @since 19-Aug-2010
 *
 */
public class HttpServerTransportFactory extends AbstractHTTPTransportFactory implements DestinationFactory
{
   private Map<String, HttpServerDestination> destinations = new ConcurrentHashMap<String, HttpServerDestination>();
   
   public HttpServerTransportFactory()
   {
      super();
   }

   @Resource
   public void setBus(Bus b)
   {
      super.setBus(b);
   }

   @PostConstruct
   public void finalizeConfig()
   {
      if (null == bus)
      {
         return;
      }
      // This call will register the server engine factory
      // with the Bus.
      getServerEngineFactory();
   }
   
   protected HttpServerEngineFactory getServerEngineFactory()
   {
      HttpServerEngineFactory serverEngineFactory = getBus().getExtension(HttpServerEngineFactory.class);
      // If it's not there, then create it and register it.
      // Spring may override it later, but we need it here for default
      // with no spring configuration.
      if (serverEngineFactory == null)
      {
         serverEngineFactory = new HttpServerEngineFactory(bus);
         serverEngineFactory.setBus(getBus());
      }
      return serverEngineFactory;
   }

   public Destination getDestination(EndpointInfo endpointInfo) throws IOException
   {
      String addr = endpointInfo.getAddress();
      HttpServerDestination destination = addr == null ? null : destinations.get(addr);
      if (destination == null)
      {
         destination = createDestination(endpointInfo);
      }
      return destination;
   }
   
   private synchronized HttpServerDestination createDestination(EndpointInfo endpointInfo) throws IOException
   {
      String addr = endpointInfo.getAddress();
      HttpServerDestination destination = addr == null ? null : destinations.get(addr);
      if (destination == null)
      {
         destination = new HttpServerDestination(getBus(), this, endpointInfo);
         destinations.put(endpointInfo.getAddress(), destination);
         configure(destination);
         destination.finalizeConfig();
      }
      return destination;
   }

   /**
    * This function removes the destination for a particular endpoint.
    */
   void removeDestination(EndpointInfo ei)
   {
      destinations.remove(ei.getAddress());
   }

}
