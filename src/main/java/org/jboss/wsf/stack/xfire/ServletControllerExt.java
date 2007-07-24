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
package org.jboss.wsf.stack.xfire;

//$Id$

import java.io.IOException;
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

   private ServletTransportFactory transport;
   private CXFServlet cxfServlet;

   public ServletControllerExt(ServletTransportFactory transport, CXFServlet servlet)
   {
      super(transport, servlet);
      this.transport = transport;
      this.cxfServlet = servlet;
   }

   public void invoke(HttpServletRequest req, HttpServletResponse res) throws ServletException
   {
      try
      {
         // Find destination based on request URI
         String requestURI = req.getRequestURI();
         ServletDestination dest = null;
         Collection<ServletDestination> destinations = transport.getDestinations();
         for (ServletDestination aux : destinations)
         {
            EndpointInfo ei = aux.getEndpointInfo();
            String address = ei.getAddress();
            
            // Fix invalid leading slash
            if (address.startsWith("/http://"))
            {
               address = address.substring(1);
               ei.setAddress(address);
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
               dest = aux;
               break;
            }
         }
         if (dest == null)
            throw new ServletException("Cannot obtain destination for: " + requestURI);

         EndpointInfo ei = dest.getEndpointInfo();
         Bus bus = cxfServlet.getBus();
         if (null != req.getQueryString() && req.getQueryString().length() > 0 && bus.getExtension(QueryHandlerRegistry.class) != null)
         {
            String ctxUri = requestURI; //req.getPathInfo();
            String baseUri = req.getRequestURL().toString() + "?" + req.getQueryString();

            for (QueryHandler qh : bus.getExtension(QueryHandlerRegistry.class).getHandlers())
            {
               if (qh.isRecognizedQuery(baseUri, ctxUri, ei))
               {

                  res.setContentType(qh.getResponseContentType(baseUri, ctxUri));
                  OutputStream out = res.getOutputStream();
                  try
                  {
                     qh.writeResponse(baseUri, ctxUri, ei, out);
                     out.flush();
                     return;
                  }
                  catch (Exception e)
                  {
                     throw new ServletException(e);
                  }
               }
            }
         }

         invokeDestination(req, res, dest);
      }
      catch (IOException e)
      {
         throw new ServletException(e);
      }
   }
}
