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

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
   private static final long serialVersionUID = -1820187716558491952L;
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
