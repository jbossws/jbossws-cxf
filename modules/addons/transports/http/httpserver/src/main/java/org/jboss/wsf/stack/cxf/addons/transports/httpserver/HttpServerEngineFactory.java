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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;

import org.apache.cxf.Bus;
import org.apache.cxf.buslifecycle.BusLifeCycleListener;
import org.apache.cxf.buslifecycle.BusLifeCycleManager;
import org.apache.cxf.common.logging.LogUtils;

/**
 * A server engine factory for the JDK6-based httpserver engine
 *
 * @author Magesh Kumar B <mageshbk@jboss.com> (C) 2011 Red Hat Inc.
 * @author alessio.soldano@jboss.com
 * @since 19-Aug-2010
 *
 */
public class HttpServerEngineFactory implements BusLifeCycleListener
{
   private static final Logger LOG = LogUtils.getL7dLogger(HttpServerEngineFactory.class);
   private static Map<Integer, HttpServerEngine> portMap = new HashMap<Integer, HttpServerEngine>();

   private BusLifeCycleManager lifeCycleManager;
   private Bus bus;

   public HttpServerEngineFactory(Bus b)
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
         bus.setExtension(this, HttpServerEngineFactory.class);
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
   public synchronized HttpServerEngine retrieveHttpServerEngine(int port)
   {
      HttpServerEngine engine = null;
      synchronized(portMap)
      {
         engine = portMap.get(port);
      }
      return engine;
   }

   public synchronized HttpServerEngine createHttpServerEngine(String host, int port, String protocol)
         throws IOException
   {
      LOG.fine("Creating HttpServer Engine for port " + port + ".");
      HttpServerEngine ref = null;
      synchronized(portMap)
      {
         ref = retrieveHttpServerEngine(port);
         if (null == ref)
         {
            ref = new HttpServerEngine(this, bus, host, port);
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
         HttpServerEngine ref = portMap.remove(port);
         if (ref != null)
         {
            LOG.fine("Stopping HttpServer Engine on port " + port + ".");
            try
            {
               ref.stop();
            }
            catch (Exception e)
            {
               LOG.log(Level.WARNING, "", e);
            }
         }
      }
   }

   public void initComplete()
   {
      // do nothing here
   }

   public void postShutdown()
   {
      // shut down the httpserver in the portMap
      // To avoid the CurrentModificationException,
      // do not use portMap.vaules directly
      HttpServerEngine[] engines = null;
      synchronized (portMap) {
         engines = portMap.values().toArray(new HttpServerEngine[0]);
      }
      for (HttpServerEngine engine : engines)
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
