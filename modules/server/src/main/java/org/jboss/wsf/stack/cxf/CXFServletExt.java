/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cxf.BusFactory;
import org.apache.cxf.resource.ResourceManager;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.apache.cxf.transport.servlet.ServletContextResourceResolver;
import org.apache.cxf.transport.servlet.ServletController;
import org.apache.cxf.transport.servlet.ServletTransportFactory;
import org.jboss.wsf.spi.deployment.Endpoint;
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
public class CXFServletExt extends CXFServlet
{
   protected Endpoint endpoint;

   @Override
   public ServletController createServletController(ServletConfig servletConfig)
   {
      ServletTransportFactory stf = (ServletTransportFactory) createServletTransportFactory();
      return new ServletControllerExt(stf, servletConfig, servletConfig.getServletContext(), bus);
   }

   @Override
   public void loadBus(ServletConfig servletConfig) throws ServletException
   {
      //Init the Endpoint
      endpoint = ServletHelper.initEndpoint(servletConfig, getServletName());

      //keep the bus created during deployment and update it with the information coming from the servlet config
      updateAvailableBusWithServletInfo(servletConfig);

      //register the InstrumentManagementImpl
      ServletHelper.registerInstrumentManger(bus, getServletContext());
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
      replaceDestinationFactory();
      //set up the ServletController as the CXF servlet would have done
      controller = createServletController(servletConfig);
      //set the controller in the servlet context now that the bus has been configured in the servlet
      servletConfig.getServletContext().setAttribute(ServletController.class.getName(), getController());
   }

   @Override
   protected void invoke(HttpServletRequest req, HttpServletResponse res) throws ServletException
   {
      ServletHelper.callRequestHandler(req, res, getServletContext(), getBus(), endpoint);
   }
   
   @Override
   public void destroy() 
   {
      ServletHelper.callPreDestroy(endpoint);
   }
   

}
