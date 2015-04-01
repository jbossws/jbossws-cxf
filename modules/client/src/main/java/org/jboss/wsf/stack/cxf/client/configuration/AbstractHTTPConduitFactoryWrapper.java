/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf.client.configuration;

import java.io.IOException;

import org.apache.cxf.Bus;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transport.http.HTTPConduitFactory;
import org.apache.cxf.transport.http.HTTPTransportFactory;
import org.apache.cxf.ws.addressing.EndpointReferenceType;

/**
 * Abstract wrapper over HTTPConduitFactory for configuring HTTPConduit instances with default
 * values just after they've been created.
 * This class can be extended to establish and set default HTTPConduit values in different ways.
 * 
 * @author alessio.soldano@jboss.com
 * @since 1-Apr-2015
 */
public abstract class AbstractHTTPConduitFactoryWrapper implements HTTPConduitFactory
{
   private HTTPConduitFactory delegate;
   
   /**
    * Installs the current wrapper in the specified Bus instance.
    * 
    * @param bus    The Bus instance to install the wrapper in
    */
   public void install(Bus bus)
   {
      delegate = bus.getExtension(HTTPConduitFactory.class);
      bus.setExtension(this, HTTPConduitFactory.class);
   }

   @Override
   public HTTPConduit createConduit(HTTPTransportFactory f, Bus b, EndpointInfo localInfo, EndpointReferenceType target)
         throws IOException
   {
      HTTPConduit conduit = null;
      if (delegate != null)
      {
         conduit = delegate.createConduit(f, b, localInfo, target);
      }
      else
      {
         conduit = createNewConduit(f, b, localInfo, target);
      }
      if (conduit != null)
      {
         configureConduit(conduit);
      }
      return conduit;
   }
   
   /**
    * Returns the HTTPConduitFactory instance that this wrapper delegates to
    * 
    * @return The wrapper's delegate
    */
   public HTTPConduitFactory getDelegate() 
   {
      return delegate;
   }

   /**
    * Creates a new HTTPConduit instance; this is used internally when no delegate is available for getting a HTTPConduit instance to configure
    * 
    * @param f          The current HTTPTransportFactory
    * @param b          The current Bus
    * @param localInfo  The current EndpointInfo
    * @param target     The EndpointReferenceType
    * @return           A new HTTPConduit instance
    * @throws IOException
    */
   protected abstract HTTPConduit createNewConduit(HTTPTransportFactory f, Bus b, EndpointInfo localInfo,
         EndpointReferenceType target) throws IOException;

   /**
    * Configures the specified HTTPConduit instance with default values
    * 
    * @param conduit    The HTTPConduit instance to be configured
    */
   protected abstract void configureConduit(HTTPConduit conduit);
}
