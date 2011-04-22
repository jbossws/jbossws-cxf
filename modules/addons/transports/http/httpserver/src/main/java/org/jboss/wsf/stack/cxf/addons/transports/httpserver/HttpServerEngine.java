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
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.jboss.ws.httpserver_httpspi.PathUtils;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * A server engine that internally uses the JDK6 httpserver
 * 
 * @author alessio.soldano@jboss.com
 * @since 19-Aug-2010
 *
 */
@SuppressWarnings("restriction")
public class HttpServerEngine
{
   private static final Logger LOG = LogUtils.getL7dLogger(HttpServerEngine.class);
   private static final int DELAY = Integer.getInteger(System.getProperty(HttpServerEngineFactory.class.getName() + ".STOP_DELAY"), 1);
   private static final int BACKLOG = 0;

   private Bus bus;
   private HttpServerEngineFactory factory;
   private String host;
   private int port;
   private int handlerCount;
   private String protocol = "http";
   private HttpServer server;

   public HttpServerEngine(HttpServerEngineFactory fac, Bus bus, String host, int port)
   {
      this.bus = bus;
      this.factory = fac;
      this.host = host;
      this.port = port;
   }
   
   public Bus getBus()
   {
      return bus;
   }

   public String getProtocol()
   {
      return protocol;
   }

   public int getPort()
   {
      return port;
   }

   public String getHost()
   {
      return host;
   }

   public synchronized void addHandler(String address, HttpHandler handler)
   {
      if (server == null) //start the server on first call
      {
         InetSocketAddress isa = host != null ? new InetSocketAddress(host, port) : new InetSocketAddress(port);
         try
         {
            server = HttpServer.create(isa, BACKLOG);
            server.start();
         }
         catch (IOException e)
         {
            throw new RuntimeException(e);
         }
      }
      server.createContext(PathUtils.getContextPath(address) + PathUtils.getPath(address), handler);
      handlerCount++;
   }

   public synchronized void removeHandler(String address)
   {
      server.removeContext(PathUtils.getContextPath(address) + PathUtils.getPath(address));
      handlerCount--;
   }

   /**
    * This method is called by the ServerEngine Factory to destroy the server
    */
   protected void stop() throws Exception
   {
      if (server != null)
      {
         server.stop(DELAY);
      }
   }

   /**
    * This method will shut down the server engine and
    * remove it from the factory's cache. 
    */
   public void shutdown()
   {
      if (factory != null && handlerCount == 0)
      {
         factory.destroyForPort(port);
      }
      else
      {
         LOG.log(Level.WARNING, "FAILED_TO_SHUTDOWN_ENGINE_MSG", port);
      }
   }
}
