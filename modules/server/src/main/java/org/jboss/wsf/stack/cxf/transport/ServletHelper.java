/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf.transport;

import static org.jboss.ws.common.integration.WSHelper.isJaxwsJseEndpoint;

import java.io.IOException;
import java.util.List;

import javax.management.ObjectName;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.handler.Handler;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.jaxws.support.JaxWsEndpointImpl;
import org.jboss.ws.common.ObjectNameFactory;
import org.jboss.ws.common.injection.InjectionHelper;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.Reference;
import org.jboss.wsf.spi.invocation.EndpointAssociation;
import org.jboss.wsf.spi.invocation.RequestHandler;
import org.jboss.wsf.spi.management.EndpointRegistry;
import org.jboss.wsf.spi.management.EndpointRegistryFactory;
import org.jboss.wsf.stack.cxf.Messages;

/**
 *
 * @author alessio.soldano@jboss.com
 * @since 16-Jun-2010
 *
 */
public class ServletHelper
{
   public static Endpoint initEndpoint(ServletConfig servletConfig, String servletName)
   {
      final ClassLoader cl = ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader();
      final EndpointRegistry epRegistry = SPIProvider.getInstance().getSPI(EndpointRegistryFactory.class, cl).getEndpointRegistry();

      ServletContext context = servletConfig.getServletContext();
      String contextPath = context.getContextPath();
      context.setAttribute(ServletConfig.class.getName(), servletConfig);
      return initServiceEndpoint(epRegistry, contextPath, servletName);
   }

   /** Initialize the service endpoint
    */
   private static Endpoint initServiceEndpoint(EndpointRegistry epRegistry, String contextPath, String servletName)
   {
      if (contextPath.startsWith("/"))
         contextPath = contextPath.substring(1);

      final ObjectName oname = ObjectNameFactory.create(Endpoint.SEPID_DOMAIN + ":" + Endpoint.SEPID_PROPERTY_CONTEXT
            + "=" + contextPath + "," + Endpoint.SEPID_PROPERTY_ENDPOINT + "=" + servletName);
      Endpoint endpoint = epRegistry.getEndpoint(oname);
      if (endpoint == null)
      {
         throw Messages.MESSAGES.cannotObtainEndpoint(oname);
      }

      //Inject the EJB and JNDI resources if possible
      injectServiceAndHandlerResources(endpoint);

      return endpoint;
   }

   private static void injectServiceAndHandlerResources(Endpoint endpoint)
   {
      ServerFactoryBean factory = endpoint.getAttachment(ServerFactoryBean.class);
      if (factory != null)
      {
         @SuppressWarnings("rawtypes")
         List<Handler> chain = ((JaxWsEndpointImpl) factory.getServer().getEndpoint()).getJaxwsBinding()
               .getHandlerChain();
         if (chain != null)
         {
            for (Handler<?> handler : chain)
            {
               final Reference handlerReference = endpoint.getInstanceProvider().getInstance(handler.getClass().getName());
               if (!handlerReference.isInitialized()) {
                   final Object handlerInstance = handlerReference.getValue();
                   InjectionHelper.callPostConstructMethod(handlerInstance);
                   handlerReference.setInitialized();
               }
            }
         }
      }
   }

   public static void callPreDestroy(Endpoint endpoint)
   {
   }

   public static void callRequestHandler(HttpServletRequest req, HttpServletResponse res, ServletContext ctx, Bus bus,
         Endpoint endpoint) throws ServletException
   {
      try
      {
         BusFactory.setThreadDefaultBus(bus);
         //set the current endpoint into the threadlocal association that is later
         //used by the EndpointAssociationInterceptor for linking the message exchange
         //related to this invocation to the proper endpoint serving it (the bus, and
         //hence the interceptor, can span multiple invocation related to multiple
         //endpoints)
         EndpointAssociation.setEndpoint(endpoint);
         RequestHandler requestHandler = (RequestHandler) endpoint.getRequestHandler();
         requestHandler.handleHttpRequest(endpoint, req, res, ctx);
      }
      catch (IOException ioe)
      {
         throw new ServletException(ioe);
      }
      finally
      {
         EndpointAssociation.removeEndpoint();
         BusFactory.setThreadDefaultBus(null);
      }
   }
}
