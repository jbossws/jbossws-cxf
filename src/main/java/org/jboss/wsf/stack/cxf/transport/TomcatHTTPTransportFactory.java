/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.wsf.stack.cxf.transport;

// $Id$

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.cxf.transport.http.AbstractHTTPTransportFactory;

public class TomcatHTTPTransportFactory extends AbstractHTTPTransportFactory implements DestinationFactory
{
   private Map<String, TomcatHTTPDestination> destinations = new HashMap<String, TomcatHTTPDestination>();

   public TomcatHTTPTransportFactory()
   {
      super();
   }

   public Destination getDestination(EndpointInfo endpointInfo) throws IOException
   {
      String addr = endpointInfo.getAddress();
      TomcatHTTPDestination destination = destinations.get(addr);
      if (destination == null)
      {
         destination = createDestination(endpointInfo);
      }

      return destination;
   }

   private synchronized TomcatHTTPDestination createDestination(EndpointInfo endpointInfo) throws IOException
   {

      TomcatHTTPDestination destination = destinations.get(endpointInfo.getAddress());
      if (destination == null)
      {
         destination = new TomcatHTTPDestination(getBus(), this, endpointInfo, true);

         destinations.put(endpointInfo.getAddress(), destination);

         configure(destination);
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
