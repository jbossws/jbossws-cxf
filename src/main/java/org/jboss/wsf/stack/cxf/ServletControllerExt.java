/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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

//$Id$

import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cxf.Bus;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.apache.cxf.transport.servlet.ServletController;
import org.apache.cxf.transport.servlet.ServletDestination;
import org.apache.cxf.transport.servlet.ServletTransportFactory;
import org.apache.cxf.transports.http.QueryHandler;
import org.apache.cxf.transports.http.QueryHandlerRegistry;
import org.jboss.logging.Logger;

/**
 * An extension to the CXF servlet controller
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 21-Apr-2007
 */
public class ServletControllerExt extends ServletController
{
   private static Logger log = Logger.getLogger(ServletControllerExt.class);

   private ServletTransportFactory cxfTransport;
   private CXFServlet cxfServlet;

   public ServletControllerExt(ServletTransportFactory cxfTransport, CXFServlet cxfServlet)
   {
      super(cxfTransport, cxfServlet);
      this.cxfTransport = cxfTransport;
      this.cxfServlet = cxfServlet;
   }
   
   /**
    * Finds destination based on request URI
    * @param requestURI to be recognized
    * @return destination associated with the request URI
    * @throws ServletException when destination wasn't found
    */
   private ServletDestination findDestination(HttpServletRequest req) throws ServletException
   {
      // Find destination based on request URI
      String requestURI = req.getRequestURI();
      Collection<ServletDestination> destinations = cxfTransport.getDestinations();
      for (ServletDestination destination : destinations)
      {
         EndpointInfo endpointInfo = destination.getEndpointInfo();
         String address = endpointInfo.getAddress();
         
         // Fix invalid leading slash
         if (address.startsWith("/http://"))
         {
            address = address.substring(1);
            endpointInfo.setAddress(address);
         }
         
         String path = address;
         try
         {
            path = new URL(address).getPath();
         }
         catch (MalformedURLException ex)
         {
            // ignore
         }
         
         if (requestURI.startsWith(path))
         {
            return destination;
         }
      }

      throw new ServletException("Cannot obtain destination for: " + requestURI);
   }
   
   /**
    * When request includes query it tries to lookup the query handler and tries to handle the request message
    * @param req request
    * @param res response
    * @param dest destination
    * @return true if there was a query handler that successfully handled the request, false otherwise
    * @throws ServletException if some problem occurs
    */
   private boolean handleQuery(HttpServletRequest req, HttpServletResponse res, ServletDestination dest)
   throws ServletException
   {
      Bus bus = cxfServlet.getBus();
      boolean hasQuery = (null != req.getQueryString()) && (req.getQueryString().length() > 0);
      boolean queryHandlerRegistryExists = bus.getExtension(QueryHandlerRegistry.class) != null;
      
      if (hasQuery && queryHandlerRegistryExists)
      {
         String ctxUri = req.getRequestURI();
         String baseUri = req.getRequestURL().toString() + "?" + req.getQueryString();
         EndpointInfo endpointInfo = dest.getEndpointInfo();

         for (QueryHandler queryHandler : bus.getExtension(QueryHandlerRegistry.class).getHandlers())
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
         
      return false;
   }

   public void invoke(HttpServletRequest req, HttpServletResponse res) throws ServletException
   {
      ServletDestination dest = findDestination(req);
      boolean requestHandled = handleQuery(req, res, dest); 
      
      if (false == requestHandled)
      {
         invokeDestination(req, res, dest);
      }
   }
}
