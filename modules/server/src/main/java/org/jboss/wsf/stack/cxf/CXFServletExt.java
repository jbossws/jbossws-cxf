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

import javax.servlet.FilterChain;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.resource.ResourceManager;
import org.apache.cxf.transport.servlet.AbstractHTTPServlet;
import org.apache.cxf.transport.servlet.ServletContextResourceResolver;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.ServletDelegate;
import org.jboss.wsf.stack.cxf.configuration.BusHolder;
import org.jboss.wsf.stack.cxf.transport.ServletHelper;

/**
 * An extension to the CXF servlet
 * 
 * @author Thomas.Diesler@jboss.org
 * @author alessio.soldano@jboss.com
 * 
 * @since 21-Apr-2007
 */
public class CXFServletExt extends AbstractHTTPServlet implements ServletDelegate
{
   protected Endpoint endpoint;
   protected Bus bus;

   @Override
   public void init(ServletConfig sc) throws ServletException {
       super.init(sc);
       loadBus(sc);
   }

   protected void loadBus(ServletConfig servletConfig) throws ServletException
   {
      //Init the Endpoint
      endpoint = ServletHelper.initEndpoint(servletConfig, getServletName());

      //keep the bus created during deployment and update it with the information coming from the servlet config
      updateAvailableBusWithServletInfo(servletConfig);
   }

   private void updateAvailableBusWithServletInfo(ServletConfig servletConfig)
   {
      BusHolder holder = endpoint.getService().getDeployment().getAttachment(BusHolder.class);
      //set the bus from deployment into the CXF servlet and assign it to the current thread (do not touch the default bus!)
      bus = holder.getBus();
      BusFactory.setThreadDefaultBus(bus);
      //update the resource manager adding the ServletContextResourceResolver that was to be added by CXF servlet
      ResourceManager resourceManager = bus.getExtension(ResourceManager.class);
      resourceManager.addResourceResolver(new ServletContextResourceResolver(servletConfig.getServletContext()));
   }

   @Override
   protected void invoke(HttpServletRequest req, HttpServletResponse res) throws ServletException
   {
      ServletHelper.callRequestHandler(req, res, getServletContext(), bus, endpoint);
   }
   
   @Override
   public void destroy() 
   {
      ServletHelper.callPreDestroy(endpoint);
   }
   
   @Override
   public void doHead(HttpServletRequest request, HttpServletResponse response, ServletContext context)
         throws ServletException, IOException
   {
      this.doHead(request, response);
   }

   @Override
   public void doGet(HttpServletRequest request, HttpServletResponse response, ServletContext context)
         throws ServletException, IOException
   {
      this.doGet(request, response);
   }

   @Override
   public void doPost(HttpServletRequest request, HttpServletResponse response, ServletContext context)
         throws ServletException, IOException
   {
      this.doPost(request, response);
   }

   @Override
   public void doPut(HttpServletRequest request, HttpServletResponse response, ServletContext context)
         throws ServletException, IOException
   {
      this.doPut(request, response);
   }

   @Override
   public void doDelete(HttpServletRequest request, HttpServletResponse response, ServletContext context)
         throws ServletException, IOException
   {
      this.doDelete(request, response);
   }

   @Override
   public void service(HttpServletRequest request, HttpServletResponse response, ServletContext context)
         throws ServletException, IOException
   {
      this.service(request, response);
   }

   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
         ServletException
   {
      // filtering not supported, move on
      chain.doFilter(request, response);
   }

   protected Bus getBus()
   {
      return bus;
   }
}
