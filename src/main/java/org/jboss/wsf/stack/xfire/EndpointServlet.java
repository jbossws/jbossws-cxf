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

import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.management.EndpointRegistry;
import org.jboss.wsf.spi.management.EndpointRegistryFactory;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.invocation.RequestHandler;
import org.jboss.wsf.spi.invocation.EndpointAssociation;
import org.jboss.wsf.common.ObjectNameFactory;

import javax.management.ObjectName;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.xml.ws.WebServiceException;
import java.io.IOException;

/**
 * @author Heiko.Braun@jboss.com
 *         Created: Jul 24, 2007
 */
public class EndpointServlet extends HttpServlet
{
   protected Endpoint endpoint;
   protected EndpointRegistry epRegistry;

   public void init(ServletConfig servletConfig) throws ServletException
   {
      super.init(servletConfig);
      SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
      epRegistry = spiProvider.getSPI(EndpointRegistryFactory.class).createEndpointRegistry();
   }

   public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      if (endpoint == null)
      {
         String contextPath = req.getContextPath();
         initServiceEndpoint(contextPath);
      }

      try
      {
         EndpointAssociation.setEndpoint(endpoint);
         RequestHandler requestHandler = endpoint.getRequestHandler();
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
      WebAppResolver resolver = new WebAppResolver(contextPath, getServletName());
      this.endpoint = epRegistry.resolve(resolver);

      if (this.endpoint == null)
      {
         ObjectName oname = ObjectNameFactory.create(Endpoint.SEPID_DOMAIN + ":" +
           Endpoint.SEPID_PROPERTY_CONTEXT + "=" + contextPath + "," +
           Endpoint.SEPID_PROPERTY_ENDPOINT + "=" + getServletName()
         );
         throw new WebServiceException("Cannot obtain endpoint for: " + oname);
      }

      // Set the runtime classloader for JSE endpoints, this should be the tomcat classloader
      Deployment dep = endpoint.getService().getDeployment();
      if (dep.getDeploymentType() == Deployment.DeploymentType.JAXRPC_JSE || dep.getDeploymentType() == Deployment.DeploymentType.JAXWS_JSE)
      {
         ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
         dep.setRuntimeClassLoader(classLoader);
      }
   }
}
