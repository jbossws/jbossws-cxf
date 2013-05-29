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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

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
import org.apache.cxf.transports.http.QueryHandler;
import org.apache.cxf.transports.http.QueryHandlerRegistry;
import org.jboss.util.NotImplementedException;
import org.jboss.ws.common.management.AbstractServerConfig;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.invocation.InvocationContext;
import org.jboss.wsf.spi.invocation.RequestHandler;
import org.jboss.wsf.spi.management.EndpointMetrics;
import org.jboss.wsf.spi.management.ServerConfig;
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
   private static RequestHandlerImpl me = new RequestHandlerImpl();
   
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
      Bus bus = ep.getService().getDeployment().getAttachment(BusHolder.class).getBus();
      AbstractHTTPDestination dest = findDestination(req, bus);
      
      boolean requestHandled = handleQuery(req, res, dest, bus); 
      if (false == requestHandled)
      {
         Long beginTime = initRequestMetrics(ep);
         HttpServletResponseWrapper response = new HttpServletResponseWrapper(res);
         try
         {
            ServletConfig cfg = (ServletConfig)context.getAttribute(ServletConfig.class.getName());
            dest.invoke(cfg, context, req, response);
         }
         catch (IOException e)
         {
            throw new ServletException(e);
         }
         if (response.getStatus() < 500)
         {
            processResponseMetrics(ep, beginTime);
         }
         else
         {
            processFaultMetrics(ep, beginTime);
         }
      }
   }

   public void handleRequest(Endpoint endpoint, InputStream inStream, OutputStream outStream, InvocationContext context)
   {
      throw new NotImplementedException();
   }

   public void handleWSDLRequest(Endpoint endpoint, OutputStream outStream, InvocationContext context)
   {
      throw new NotImplementedException();
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
      Collection<AbstractHTTPDestination> destinations = destRegistry.getDestinations();
      AbstractHTTPDestination returnValue = null;
      for (AbstractHTTPDestination destination : destinations)
      {
         EndpointInfo endpointInfo = destination.getEndpointInfo();
         String address = endpointInfo.getAddress();
         
         String path = address;
         try
         {
            path = new URL(address).getPath();
         }
         catch (MalformedURLException ex)
         {
            // ignore
         }
         
         if (path != null)
         {
            if (requestURI.equals(path))
            {
               return destination; // exact match
            }
            else if (requestURI.startsWith(path))
            {
               returnValue = destination; // fallback
            }
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
   
   /**
    * When request includes query it tries to lookup the query handler and tries to handle the request message
    * @param req request
    * @param res response
    * @param dest destination
    * @return true if there was a query handler that successfully handled the request, false otherwise
    * @throws ServletException if some problem occurs
    */
   private final boolean handleQuery(HttpServletRequest req, HttpServletResponse res, AbstractHTTPDestination dest, Bus bus)
   throws ServletException
   {
      final String queryString = req.getQueryString();
      if ((null != queryString) && (queryString.length() > 0))
      {
         final QueryHandlerRegistry qhr = bus.getExtension(QueryHandlerRegistry.class);
         if (qhr != null) {
            final String ctxUri = req.getRequestURI();
            final String baseUri = req.getRequestURL().toString() + "?" + queryString;
            final EndpointInfo endpointInfo = dest.getEndpointInfo();
            final ServerConfig serverConfig = AbstractServerConfig.getServerIntegrationServerConfig();
            if (serverConfig.isModifySOAPAddress()) {
               endpointInfo.setProperty(WSDLGetUtils.AUTO_REWRITE_ADDRESS_ALL,
                     ServerConfig.UNDEFINED_HOSTNAME.equals(serverConfig.getWebServiceHost()));
            }
   
            for (QueryHandler queryHandler : qhr.getHandlers())
            {
               if (queryHandler.isRecognizedQuery(baseUri, ctxUri, endpointInfo))
               {
                  res.setContentType(queryHandler.getResponseContentType(baseUri, ctxUri));
                  try
                  {
                     OutputStream out = res.getOutputStream();
                     queryHandler.writeResponse(baseUri, ctxUri, endpointInfo, out);
                     out.flush();
                     return true;
                  }
                  catch (Exception e)
                  {
                     throw new ServletException(e);
                  }
               }
            }
         }
      }
         
      return false;
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
