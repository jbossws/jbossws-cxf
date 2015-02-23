/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.util.Collection;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

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
import org.jboss.wsf.spi.invocation.InvocationContext;
import org.jboss.wsf.spi.invocation.RequestHandler;
import org.jboss.wsf.spi.management.EndpointMetrics;
import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.spi.metadata.config.SOAPAddressRewriteMetadata;
import org.jboss.wsf.stack.cxf.addressRewrite.SoapAddressRewriteHelper;
import org.jboss.wsf.stack.cxf.configuration.BusHolder;

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
      if (response.getStatus() < 500 && statisticsEnabled)
      {
         processResponseMetrics(ep, beginTime);
      }
      if (response.getStatus() >= 500 && statisticsEnabled)
      {
         processFaultMetrics(ep, beginTime);
      }
   }
   
   private boolean hasQueryString(HttpServletRequest req)
   {
      final String queryString = req.getQueryString();
      return ((null != queryString) && (queryString.length() > 0));
   }

   public void handleRequest(Endpoint endpoint, InputStream inStream, OutputStream outStream, InvocationContext context)
   {
      throw new RuntimeException();
   }

   public void handleWSDLRequest(Endpoint endpoint, OutputStream outStream, InvocationContext context)
   {
      throw new RuntimeException();
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
