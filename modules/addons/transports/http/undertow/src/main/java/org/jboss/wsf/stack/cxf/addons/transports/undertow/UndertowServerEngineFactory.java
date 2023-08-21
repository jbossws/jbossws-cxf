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
package org.jboss.wsf.stack.cxf.addons.transports.undertow;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.jboss.logging.Logger;

import jakarta.annotation.Resource;

import org.apache.cxf.Bus;
import org.apache.cxf.buslifecycle.BusLifeCycleListener;
import org.apache.cxf.buslifecycle.BusLifeCycleManager;

/**
 * A server engine factory for the undertow engine
 *
 * @author Magesh Kumar B <mageshbk@jboss.com> (C) 2011 Red Hat Inc.
 * @author alessio.soldano@jboss.com
 * @since 19-Aug-2010
 *
 */
public class UndertowServerEngineFactory implements BusLifeCycleListener
{
   private static final Logger LOG = Logger.getLogger(UndertowServerEngineFactory.class);
   private static final Map<Integer, UndertowServerEngine> portMap = new HashMap<Integer, UndertowServerEngine>();

   private BusLifeCycleManager lifeCycleManager;
   private Bus bus;

   public UndertowServerEngineFactory(Bus b)
   {
      setBus(b);
   }

   /**
   * This call is used to set the bus. It should only be called once.
   * @param bus
   */
   @Resource(name = "cxf")
   public final void setBus(Bus bus)
   {
      assert this.bus == null || this.bus == bus;
      this.bus = bus;
      if (bus != null)
      {
         bus.setExtension(this, UndertowServerEngineFactory.class);
         lifeCycleManager = bus.getExtension(BusLifeCycleManager.class);
         if (null != lifeCycleManager)
         {
            lifeCycleManager.registerLifeCycleListener(this);
         }
      }
   }

   public Bus getBus()
   {
      return bus;
   }

   /**
    * Retrieve a previously configured HttpServerEngine for the
    * given port. If none exists, this call returns null.
    */
   public synchronized UndertowServerEngine retrieveHttpServerEngine(int port)
   {
      UndertowServerEngine engine = null;
      synchronized(portMap)
      {
         engine = portMap.get(port);
      }
      return engine;
   }

   public synchronized UndertowServerEngine createHttpServerEngine(String host, int port, String protocol)
         throws IOException
   {
      if (LOG.isDebugEnabled())
      {
         LOG.debug("Creating HttpServer Engine for port " + port + ".");
      }
      UndertowServerEngine ref = null;
      synchronized(portMap)
      {
         ref = retrieveHttpServerEngine(port);
         if (null == ref)
         {
            ref = new UndertowServerEngine(this, bus, host, port);
            portMap.put(port, ref);
         }
         // checking the protocol
         if (!protocol.equals(ref.getProtocol()))
         {
            throw new IOException("Protocol mismatch for port " + port + ": " + "engine's protocol is "
               + ref.getProtocol() + ", the url protocol is " + protocol);
         }
      }
      return ref;
   }

   /**
    * This method removes the Server Engine from the port map and stops it.
    */
   public synchronized void destroyForPort(int port)
   {
      synchronized(portMap)
      {
         UndertowServerEngine ref = portMap.remove(port);
         if (ref != null)
         {
            if (LOG.isDebugEnabled())
            {
               LOG.debug("Stopping HttpServer Engine on port " + port + ".");
            }
            try
            {
               ref.stop();
            }
            catch (Exception e)
            {
               LOG.warn("", e);
            }
         }
      }
   }

   public void initComplete()
   {
      // do nothing here
   }

   public synchronized void postShutdown()
   {
      // shut down the engine in the portMap
      // To avoid the CurrentModificationException,
      // do not use portMap.vaules directly
      UndertowServerEngine[] engines = null;
      synchronized (portMap) {
         engines = portMap.values().toArray(new UndertowServerEngine[0]);
      }
      for (UndertowServerEngine engine : engines)
      {
         if (engine.getBus() == getBus())
         {
            engine.shutdown();
         }
      }
   }

   public void preShutdown()
   {
      // do nothing here
   }
}
