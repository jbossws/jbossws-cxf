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
   private final HTTPConduitFactory delegate;
   
   public AbstractHTTPConduitFactoryWrapper(HTTPConduitFactory delegate)
   {
      this.delegate = delegate;
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
