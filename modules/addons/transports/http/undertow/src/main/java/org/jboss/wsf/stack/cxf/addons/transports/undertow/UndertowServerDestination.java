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

import io.undertow.server.HttpServerExchange;

import java.io.IOException;
import java.net.URL;
import org.jboss.logging.Logger;

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
   private static final Logger LOG = Logger.getLogger(UndertowServerDestination.class);
   private static final java.util.logging.Logger JAVA_LOG = LogUtils.getL7dLogger(UndertowServerDestination.class);

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
   protected java.util.logging.Logger getLogger()
   {
      return JAVA_LOG;
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
      LOG.debug("Activating receipt of incoming messages");
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
      LOG.debug("Deactivating receipt of incoming messages");
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
            LOG.error(Handler.class.getName(), "handle(" + HttpServerExchange.class.getName() + " ex)", e);
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
