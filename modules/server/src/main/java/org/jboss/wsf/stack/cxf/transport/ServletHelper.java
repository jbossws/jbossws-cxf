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
package org.jboss.wsf.stack.cxf.transport;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import javax.management.ObjectName;
import javax.naming.Context;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.jaxws.support.JaxWsEndpointImpl;
import org.apache.cxf.management.InstrumentationManager;
import org.apache.cxf.management.counters.CounterRepository;
import org.jboss.ws.common.ObjectNameFactory;
import org.jboss.ws.common.injection.InjectionHelper;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.Deployment.DeploymentType;
import org.jboss.wsf.spi.invocation.EndpointAssociation;
import org.jboss.wsf.spi.invocation.RequestHandler;
import org.jboss.wsf.spi.management.EndpointRegistry;
import org.jboss.wsf.spi.management.EndpointRegistryFactory;
import org.jboss.wsf.spi.metadata.injection.InjectionsMetaData;
import org.jboss.wsf.stack.cxf.management.InstrumentationManagerExtImpl;

/**
 * 
 * @author alessio.soldano@jboss.com
 * @since 16-Jun-2010
 *
 */
public class ServletHelper
{
   public static final String ENABLE_CXF_MANAGEMENT = "enable.cxf.management";

   public static Endpoint initEndpoint(ServletConfig servletConfig, String servletName)
   {
      ClassLoader cl = ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader();
      SPIProvider spiProvider = SPIProviderResolver.getInstance(cl).getProvider();
      EndpointRegistry epRegistry = spiProvider.getSPI(EndpointRegistryFactory.class, cl).getEndpointRegistry();

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

      Endpoint endpoint = null;
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
         ObjectName oname = ObjectNameFactory.create(Endpoint.SEPID_DOMAIN + ":" + Endpoint.SEPID_PROPERTY_CONTEXT
               + "=" + contextPath + "," + Endpoint.SEPID_PROPERTY_ENDPOINT + "=" + servletName);
         throw new WebServiceException("Cannot obtain endpoint for: " + oname);
      }

      //Inject the EJB and JNDI resources if possible
      injectServiceAndHandlerResources(endpoint);

      return endpoint;
   }

   @SuppressWarnings("unchecked")
   private static void injectServiceAndHandlerResources(Endpoint endpoint)
   {
      ServerFactoryBean factory = endpoint.getAttachment(ServerFactoryBean.class);
      if (factory != null)
      {
         InjectionsMetaData metadata = endpoint.getAttachment(InjectionsMetaData.class);
         Context jndiContext = endpoint.getJNDIContext();
         List<Handler> chain = ((JaxWsEndpointImpl) factory.getServer().getEndpoint()).getJaxwsBinding()
               .getHandlerChain();
         if (chain != null)
         {
            for (Handler handler : chain)
            {
               InjectionHelper.injectResources(handler, metadata, jndiContext);
               InjectionHelper.callPostConstructMethod(handler);
            }
         }
      }
   }
   
   public static void callPreDestroy(Endpoint endpoint)
   {
      ServerFactoryBean factory = endpoint.getAttachment(ServerFactoryBean.class);
      if (factory != null)
      {
         if (DeploymentType.JAXWS_EJB3 != endpoint.getService().getDeployment().getType()
               && factory.getServiceBean() != null)
         {
            InjectionHelper.callPreDestroyMethod(factory.getServiceBean());
         }
      }
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

   public static void registerInstrumentManger(Bus bus, ServletContext svCtx) throws ServletException
   {
      //TODO!! Jim, remove reflection use inside this by providing proper hook in CXF and move this configuration to BusHolder
      if (svCtx.getInitParameter(ENABLE_CXF_MANAGEMENT) != null
            && "true".equalsIgnoreCase((String) svCtx.getInitParameter(ENABLE_CXF_MANAGEMENT)))
      {
         InstrumentationManagerExtImpl instrumentationManagerImpl = new InstrumentationManagerExtImpl();
         instrumentationManagerImpl.setBus(bus);
         instrumentationManagerImpl.setEnabled(true);
         instrumentationManagerImpl.initMBeanServer();
         instrumentationManagerImpl.register();
         bus.setExtension(instrumentationManagerImpl, InstrumentationManager.class);

         //attach couterRepository
         CounterRepository couterRepository = new CounterRepository();
         couterRepository.setBus(bus);

         try
         {
            Method method = CounterRepository.class.getDeclaredMethod("registerInterceptorsToBus", new Class[]
            {});
            method.setAccessible(true);
            method.invoke(couterRepository, new Object[]
            {});
         }
         catch (Exception e)
         {
            throw new ServletException(e);
         }
      }
   }
}
