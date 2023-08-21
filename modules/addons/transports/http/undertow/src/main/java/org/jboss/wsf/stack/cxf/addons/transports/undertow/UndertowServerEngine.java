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

import io.undertow.server.HttpHandler;

import java.net.InetSocketAddress;
import java.security.AccessController;
import java.security.PrivilegedAction;
import org.jboss.logging.Logger;

import org.apache.cxf.Bus;
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
   private static final Logger LOG = Logger.getLogger(UndertowServerEngine.class);
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
         LOG.warnv("Failed to shutdown Undertow server on port {0,number,####0} because it is still in use", port);
      }
   }
}
