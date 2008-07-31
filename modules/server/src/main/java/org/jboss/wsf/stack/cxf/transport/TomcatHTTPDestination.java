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

import java.io.IOException;
import java.util.logging.Logger;

import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

public class TomcatHTTPDestination extends AbstractHTTPDestination
{
   private static final Logger LOG = LogUtils.getL7dLogger(TomcatHTTPDestination.class);
   
   private TomcatHTTPTransportFactory transportFactory;
   
   public TomcatHTTPDestination(Bus b, TomcatHTTPTransportFactory ci, EndpointInfo ei, boolean dp) throws IOException
   {
      super(b, ci, ei, dp);
      this.transportFactory = ci;
   }

   @Override
   protected Logger getLogger()
   {
      return LOG;
   }
   
   @Override
   public void shutdown() {
       transportFactory.removeDestination(endpointInfo);
       super.shutdown();
   }
}
