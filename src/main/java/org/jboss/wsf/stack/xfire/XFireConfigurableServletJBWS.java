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
import java.net.URL;

import javax.management.ObjectName;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.WebServiceException;

import org.codehaus.xfire.XFire;
import org.codehaus.xfire.transport.http.XFireConfigurableServlet;
import org.codehaus.xfire.transport.http.XFireServletController;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.EndpointAssociation;
import org.jboss.wsf.spi.invocation.RequestHandler;
import org.jboss.wsf.spi.management.EndpointRegistry;
import org.jboss.wsf.spi.management.EndpointRegistryFactory;
import org.jboss.wsf.spi.utils.ObjectNameFactory;

/**
 * An extension to the XFire servlet
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 21-Apr-2007
 */
public class XFireConfigurableServletJBWS extends XFireConfigurableServlet
{
   public static final String PARAM_XFIRE_SERVICES_URL = "jbossws.xfire.services.url";

   private final static String CONFIG_FILE = "/WEB-INF/classes/META-INF/xfire/services.xml";

   protected Endpoint endpoint;
   protected EndpointRegistry epRegistry;

   public void init(ServletConfig servletConfig) throws ServletException
   {
      super.init(servletConfig);

      // Init the Endpoint
      epRegistry = EndpointRegistryFactory.getEndpointRegistry();
      String contextPath = servletConfig.getServletContext().getContextPath();
      initServiceEndpoint(contextPath);
      endpoint.addAttachment(XFireServletController.class, controller);
   }

   public XFire createXFire() throws ServletException
   {
      XFire xfire;
      try
      {
         // #1 Load services.xml from default location
         ServletContext context = getServletContext();
         URL servicesURL = context.getResource(CONFIG_FILE);

         // #1 Load services.xml from init parameter
         if (servicesURL == null)
         {
            String paramValue = context.getInitParameter(PARAM_XFIRE_SERVICES_URL);
            if (paramValue != null)
               servicesURL = new URL(paramValue);
         }

         xfire = loadConfig(servicesURL.getFile());
      }
      catch (Exception e)
      {
         throw new ServletException(e);
      }

      return xfire;
   }

   public XFireServletController createController() throws ServletException
   {
      return new XFireServletControllerJBWS(xfire, getServletContext());
   }

   public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      try
      {
         EndpointAssociation.setEndpoint(endpoint);
         RequestHandler requestHandler = (RequestHandler)endpoint.getRequestHandler();
         requestHandler.handleHttpRequest(endpoint, req, res, getServletContext());
      }
      finally
      {
         EndpointAssociation.removeEndpoint();
      }
   }

   /** Initialize the service endpoint
    */
   protected void initServiceEndpoint(String contextPath)
   {
      if (contextPath.startsWith("/"))
         contextPath = contextPath.substring(1);

      String servletName = getServletName();
      for (ObjectName sepId : epRegistry.getEndpoints())
      {
         String propContext = sepId.getKeyProperty(Endpoint.SEPID_PROPERTY_CONTEXT);
         String propEndpoint = sepId.getKeyProperty(Endpoint.SEPID_PROPERTY_ENDPOINT);
         if (servletName.equals(propEndpoint) && contextPath.equals(propContext))
         {
            endpoint = epRegistry.getEndpoint(sepId);
            break;
         }
      }

      if (endpoint == null)
      {
         ObjectName oname = ObjectNameFactory.create(Endpoint.SEPID_DOMAIN + ":" + Endpoint.SEPID_PROPERTY_CONTEXT + "=" + contextPath + ","
               + Endpoint.SEPID_PROPERTY_ENDPOINT + "=" + servletName);
         throw new WebServiceException("Cannot obtain endpoint for: " + oname);
      }
   }
}
