/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf.addons.transports.undertow;

import io.undertow.server.HttpHandler;

import java.net.InetSocketAddress;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.jboss.ws.undertow_httpspi.PathUtils;
import org.jboss.ws.undertow_httpspi.UndertowServer;

/**
 * A server engine that internally uses Undertow
 * 
 * @author alessio.soldano@jboss.com
 * @author <a href="mailto:ema@redhat.com">Jim Ma</a>
 *
 */
public class UndertowServerEngine
{
   private static final RuntimePermission START_UNDERTOW_SERVER_ENGINE = new RuntimePermission("org.jboss.ws.START_UNDERTOW_SERVER_ENGINE");
   private static final Logger LOG = LogUtils.getL7dLogger(UndertowServerEngine.class);
   private Bus bus;
   private UndertowServerEngineFactory factory;
   private String host;
   private int port;
   private int handlerCount;
   private String protocol = "http";
   private UndertowServer server;

   public UndertowServerEngine(UndertowServerEngineFactory fac, Bus bus, String host, int port)
   {
      this.bus = bus;
      this.factory = fac;
      this.host = host;
      this.port = port;
   }

   public synchronized Bus getBus()
   {
      return bus;
   }

   public synchronized String getProtocol()
   {
      return protocol;
   }

   public synchronized int getPort()
   {
      return port;
   }

   public synchronized String getHost()
   {
      return host;
   }

   public synchronized void addHandler(String address, HttpHandler handler)
   {
      if (server == null) //start the server on first call
      {
         InetSocketAddress isa = host != null ? new InetSocketAddress(host, port) : new InetSocketAddress(port);

         server = new UndertowServer(isa.getPort(), isa.getHostName());
         server.getPathHandler().addExactPath(PathUtils.getContextPath(address) + PathUtils.getPath(address), handler);
         final SecurityManager sm = System.getSecurityManager();
         if (sm == null) {
            server.start();
         } else {
             sm.checkPermission(START_UNDERTOW_SERVER_ENGINE);
             AccessController.doPrivileged(new PrivilegedAction<Object>() {
                 public Object run() {
                    server.start();
                    return null;
                 }
             });
         }
      }
      server.getPathHandler().addExactPath(PathUtils.getContextPath(address) + PathUtils.getPath(address), handler);
      
      handlerCount++;
   }

   public synchronized void removeHandler(String address)
   {
      server.getPathHandler().removeExactPath(PathUtils.getContextPath(address) + PathUtils.getPath(address));
      handlerCount--;
   }

   /**
    * This method is called by the ServerEngine Factory to destroy the server
    */
   protected synchronized void stop() throws Exception
   {
      if (server != null)
      {
         server.stop();
      }
   }

   /**
    * This method will shut down the server engine and
    * remove it from the factory's cache. 
    */
   public synchronized void shutdown()
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
