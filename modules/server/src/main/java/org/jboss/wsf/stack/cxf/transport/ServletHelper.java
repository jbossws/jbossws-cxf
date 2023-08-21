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
package org.jboss.wsf.stack.cxf.transport;

import java.io.IOException;
import java.util.List;

import javax.management.ObjectName;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.ws.handler.Handler;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.jaxws.support.JaxWsEndpointImpl;
import org.jboss.ws.common.ObjectNameFactory;
import org.jboss.ws.common.configuration.ConfigDelegateHandler;
import org.jboss.ws.common.injection.InjectionHelper;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.Reference;
import org.jboss.wsf.spi.invocation.EndpointAssociation;
import org.jboss.wsf.spi.invocation.RequestHandler;
import org.jboss.wsf.spi.management.EndpointRegistry;
import org.jboss.wsf.spi.management.EndpointRegistryFactory;
import org.jboss.wsf.stack.cxf.i18n.Messages;

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
      org.apache.cxf.endpoint.Endpoint ep = endpoint.getAttachment(org.apache.cxf.endpoint.Endpoint.class);
      if (ep != null)
      {
         @SuppressWarnings("rawtypes")
         List<Handler> chain = ((JaxWsEndpointImpl) ep).getJaxwsBinding().getHandlerChain();
         if (chain != null)
         {
            for (Handler<?> handler : chain)
            {
               if (handler instanceof ConfigDelegateHandler)
               {
                  handler = ((ConfigDelegateHandler<?>) handler).getDelegate();
               }
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
         if (endpoint.getSecurityDomainContext() != null) {
            endpoint.getSecurityDomainContext().cleanupSubjectContext();
         }
         EndpointAssociation.removeEndpoint();
         BusFactory.setThreadDefaultBus(null);
      }
   }
}
