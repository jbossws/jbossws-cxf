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

import io.undertow.server.HttpServerExchange;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.Bus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.http.DestinationRegistry;
import org.apache.cxf.transport.http_jaxws_spi.HttpHandlerImpl;
import org.apache.cxf.transport.http_jaxws_spi.JAXWSHttpSpiDestination;
import org.jboss.ws.undertow_httpspi.UndertowHttpExchange;

/**
 * HTTP destination to be used with Undertow; this extends the
 * basic JAXWSHttpSpiDestination with all the mechanisms for properly
 * handling destination and factory life-cycles.
 * 
 * @author alessio.soldano@jboss.com
 * @author <a href="mailto:ema@redhat.com">Jim Ma</a>
 * @since 19-Aug-2010
 *
 */
public class UndertowServerDestination extends JAXWSHttpSpiDestination
{
   static final Logger LOG = LogUtils.getL7dLogger(UndertowServerDestination.class);

   private UndertowServerEngineFactory serverEngineFactory;
   private UndertowServerEngine engine;
   private URL url;

   public UndertowServerDestination(Bus b, DestinationRegistry registry, EndpointInfo ei) throws IOException
   {
      super(b, registry, ei);
      this.serverEngineFactory = getServerEngineFactory();
      getAddressValue(ei, true); //generate address if not specified
      this.url = new URL(ei.getAddress());
   }

   @Override
   protected Logger getLogger()
   {
      return LOG;
   }

   public void finalizeConfig()
   {
      engine = serverEngineFactory.retrieveHttpServerEngine(url.getPort());
      if (engine == null)
      {
         try
         {
            engine = serverEngineFactory.createHttpServerEngine(url.getHost(), url.getPort(), url.getProtocol());
         }
         catch (IOException e)
         {
            throw new RuntimeException(e);
         }
      }
      if (!url.getProtocol().equals(engine.getProtocol()))
      {
         throw new IllegalStateException("Port " + engine.getPort() + " is configured with wrong protocol \""
               + engine.getProtocol() + "\" for \"" + url + "\"");
      }
   }
   
   protected UndertowServerEngineFactory getServerEngineFactory()
   {
      UndertowServerEngineFactory serverEngineFactory = getBus().getExtension(UndertowServerEngineFactory.class);
      // If it's not there, then create it and register it.
      // Spring may override it later, but we need it here for default
      // with no spring configuration.
      if (serverEngineFactory == null)
      {
         serverEngineFactory = new UndertowServerEngineFactory(bus);
      }
      return serverEngineFactory;
   }

   /**
    * Activate receipt of incoming messages.
    */
   protected void activate()
   {
      LOG.log(Level.FINE, "Activating receipt of incoming messages");
      String addr = endpointInfo.getAddress();
      try
      {
         new URL(addr);
      }
      catch (Exception e)
      {
         throw new Fault(e);
      }
      engine.addHandler(addr, new Handler(this, SecurityActions.getContextClassLoader()));
   }

   /**
    * Deactivate receipt of incoming messages.
    */
   protected void deactivate()
   {
      LOG.log(Level.FINE, "Deactivating receipt of incoming messages");
      engine.removeHandler(endpointInfo.getAddress());
   }

   class Handler extends HttpHandlerImpl implements io.undertow.server.HttpHandler
   {

      private ClassLoader classLoader;

      public Handler(JAXWSHttpSpiDestination destination, ClassLoader classLoader)
      {
         super(destination);
         this.classLoader = classLoader;
      }


      @Override
      public void handleRequest(HttpServerExchange exchange) throws Exception
      {
         ClassLoader origClassLoader = SecurityActions.getContextClassLoader();
         try
         {
            SecurityActions.setContextClassLoader(this.classLoader);
            this.handle(new UndertowHttpExchange(exchange));
         }
         catch (Exception e)
         {
            LOG.throwing(Handler.class.getName(), "handle(" + HttpServerExchange.class.getName() + " ex)", e);
            if (e instanceof IOException)
            {
               throw (IOException) e;
            }
            else
            {
               throw new RuntimeException(e);
            }
         }
         finally
         {
            SecurityActions.setContextClassLoader(origClassLoader);
         }
         
      }
   }

}
