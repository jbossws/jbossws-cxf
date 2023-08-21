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
package org.jboss.wsf.stack.cxf;

import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.util.Collection;
import java.util.regex.Pattern;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import org.apache.cxf.Bus;
import org.apache.cxf.BusException;
import org.apache.cxf.frontend.WSDLGetUtils;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.cxf.transport.DestinationFactoryManager;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.cxf.transport.http.DestinationRegistry;
import org.apache.cxf.transport.http.HTTPTransportFactory;
import org.jboss.ws.common.management.AbstractServerConfig;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.invocation.RequestHandler;
import org.jboss.wsf.spi.management.EndpointMetrics;
import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.spi.metadata.config.SOAPAddressRewriteMetadata;
import org.jboss.wsf.stack.cxf.addressRewrite.SoapAddressRewriteHelper;
import org.jboss.wsf.stack.cxf.configuration.BusHolder;
import org.jboss.wsf.stack.cxf.i18n.Messages;
import org.jboss.logging.Logger;

/**
 * A request handler
 *
 * @author Thomas.Diesler@jboss.org
 * @author alessio.soldano@jboss.com
 * @since 21-May-2007
 */
public class RequestHandlerImpl implements RequestHandler
{
   private static final RequestHandlerImpl me = new RequestHandlerImpl();
   private static final Pattern pathPattern = Pattern.compile("/{2,}");

   RequestHandlerImpl()
   {
      //NOOP
   }

   static RequestHandlerImpl getInstance()
   {
      return me;
   }

   public void handleHttpRequest(Endpoint ep, HttpServletRequest req, HttpServletResponse res, ServletContext context) throws ServletException, IOException
   {
      final boolean isGet = "GET".equals(req.getMethod());
      final boolean isGetWithQueryString = isGet && hasQueryString(req);
      if (isGet && !isGetWithQueryString)
      {
         //reject HTTP GET without query string (only support messages sent w/ POST)
         res.setStatus(405);
         res.setContentType("text/plain");
         Writer out = res.getWriter();
         out.write("HTTP GET not supported");
         out.close();
         return;
      }
      final boolean statisticsEnabled = getServerConfig().isStatisticsEnabled();
      final Long beginTime = statisticsEnabled == true ? initRequestMetrics(ep) : 0;
      final Deployment dep = ep.getService().getDeployment();
      final AbstractHTTPDestination dest = findDestination(req, dep.getAttachment(BusHolder.class).getBus());
      final HttpServletResponseWrapper response = new HttpServletResponseWrapper(res);
      try
      {
         ServletConfig cfg = (ServletConfig)context.getAttribute(ServletConfig.class.getName());
         if (isGetWithQueryString) {
            final EndpointInfo endpointInfo = dest.getEndpointInfo();
            final boolean autoRewrite = SoapAddressRewriteHelper.isAutoRewriteOn(dep.getAttachment(SOAPAddressRewriteMetadata.class));
            endpointInfo.setProperty(WSDLGetUtils.AUTO_REWRITE_ADDRESS, autoRewrite);
            endpointInfo.setProperty(WSDLGetUtils.AUTO_REWRITE_ADDRESS_ALL, autoRewrite);
         }
         dest.invoke(cfg, context, req, response);
      }
      catch (IOException e)
      {
         throw new ServletException(e);
      }
      if (statisticsEnabled && response.getStatus() < 500)
      {
         processResponseMetrics(ep, beginTime);
      }
      if (statisticsEnabled && response.getStatus() >= 500)
      {
         processFaultMetrics(ep, beginTime);
      }
   }
   
   private boolean hasQueryString(HttpServletRequest req)
   {
      final String queryString = req.getQueryString();
      return ((null != queryString) && (queryString.length() > 0));
   }

   /**
    * Finds destination based on request URI
    * @param requestURI to be recognized
    * @return destination associated with the request URI
    * @throws ServletException when destination wasn't found
    */
   private AbstractHTTPDestination findDestination(HttpServletRequest req, Bus bus) throws ServletException
   {
      // Find destination based on request URI
      String requestURI = req.getRequestURI();
      DestinationRegistry destRegistry = getDestinationRegistryFromBus(bus);
      if (destRegistry == null)
      {
         throw Messages.MESSAGES.cannotObtainRegistry(DestinationRegistry.class.getName());
      }
      requestURI = pathPattern.matcher(requestURI).replaceAll("/");
      //first try looking up the destination in the registry map
      final AbstractHTTPDestination dest = destRegistry.getDestinationForPath(requestURI, true);
      if (dest != null) {
         return dest;
      }
      //if there's no direct match, iterate on the destinations to see if there's valid "catch-all" destination
      //(servlet-based endpoints, with "/*" url-pattern in web.xml)
      Collection<AbstractHTTPDestination> destinations = destRegistry.getDestinations();
      AbstractHTTPDestination returnValue = null;
      for (AbstractHTTPDestination destination : destinations)
      {
         String path = destination.getEndpointInfo().getAddress();
         try
         {
            path = new URL(path).getPath();
         }
         catch (MalformedURLException ex)
         {
            // ignore
            Logger.getLogger(RequestHandlerImpl.class).trace(ex);
         }

         if (path != null && requestURI.startsWith(path)) {
            returnValue = destination;
         }
      }

      if (returnValue == null)
         throw Messages.MESSAGES.cannotObtainDestinationFor(requestURI);

      return returnValue;
   }

   private static DestinationRegistry getDestinationRegistryFromBus(Bus bus) throws ServletException {
      DestinationFactoryManager dfm = bus.getExtension(DestinationFactoryManager.class);
      try {
          DestinationFactory df = dfm
              .getDestinationFactory("http://cxf.apache.org/transports/http/configuration");
          if (df instanceof HTTPTransportFactory) {
              HTTPTransportFactory transportFactory = (HTTPTransportFactory)df;
              return transportFactory.getRegistry();
          }
      } catch (BusException e) {
          throw Messages.MESSAGES.cannotObtainDestinationFactoryForHttpTransport(e);
      }
      return null;
   }
   
   private static ServerConfig getServerConfig()
   {
      if (System.getSecurityManager() == null)
      {
         return AbstractServerConfig.getServerIntegrationServerConfig();
      }
      return AccessController.doPrivileged(AbstractServerConfig.GET_SERVER_INTEGRATION_SERVER_CONFIG);
   }

   private long initRequestMetrics(Endpoint endpoint)
   {
      long beginTime = 0;

      EndpointMetrics metrics = endpoint.getEndpointMetrics();
      if (metrics != null)
         beginTime = metrics.processRequestMessage();

      return beginTime;
   }

   private void processResponseMetrics(Endpoint endpoint, long beginTime)
   {
      EndpointMetrics metrics = endpoint.getEndpointMetrics();
      if (metrics != null)
         metrics.processResponseMessage(beginTime);
   }

   private void processFaultMetrics(Endpoint endpoint, long beginTime)
   {
      EndpointMetrics metrics = endpoint.getEndpointMetrics();
      if (metrics != null)
         metrics.processFaultMessage(beginTime);
   }
}
