/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
import java.lang.reflect.Method;

import javax.management.ObjectName;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.WebServiceException;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.management.InstrumentationManager;
import org.apache.cxf.management.counters.CounterRepository;
import org.apache.cxf.resource.ResourceManager;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.apache.cxf.transport.servlet.ServletContextResourceResolver;
import org.apache.cxf.transport.servlet.ServletController;
import org.apache.cxf.transport.servlet.ServletTransportFactory;
import org.jboss.wsf.common.ObjectNameFactory;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.invocation.EndpointAssociation;
import org.jboss.wsf.spi.invocation.RequestHandler;
import org.jboss.wsf.spi.management.EndpointRegistry;
import org.jboss.wsf.spi.management.EndpointRegistryFactory;
import org.jboss.wsf.stack.cxf.configuration.BusHolder;
import org.jboss.wsf.stack.cxf.management.InstrumentationManagerExtImpl;

/**
 * An extension to the CXF servlet
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 21-Apr-2007
 */
public class CXFServletExt extends CXFServlet
{
   public static final String ENABLE_CXF_MANAGEMENT = "enable.cxf.management";  
   
   protected Endpoint endpoint;
   protected EndpointRegistry epRegistry;

   @Override
   public ServletController createServletController(ServletConfig servletConfig)
   {
      ServletTransportFactory stf = (ServletTransportFactory)createServletTransportFactory();
      return new ServletControllerExt(stf, servletConfig, servletConfig.getServletContext(), bus);
   }

   @Override
   public void loadBus(ServletConfig servletConfig) throws ServletException
   {
      //Init the Endpoint
      initEndpoint(servletConfig);
      
      ServletContext svCtx = getServletContext();
      //keep the bus created during deployment and update it with the information coming from the servlet config
      updateAvailableBusWithServletInfo(servletConfig);
      
      //register the InstrumentManagementImpl
      //TODO!! remove reflection use inside this by providing proper hook in CXF and move this configuration to BusHolder
      if (svCtx.getInitParameter(ENABLE_CXF_MANAGEMENT) != null && "true".equalsIgnoreCase((String)svCtx.getInitParameter(ENABLE_CXF_MANAGEMENT))) {
         registerInstrumentManger(bus);
      }
   }
   
   private void updateAvailableBusWithServletInfo(ServletConfig servletConfig)
   {
      BusHolder holder = endpoint.getAttachment(BusHolder.class);
      //set the bus from deployment into the CXF servlet and assign it to the current thread
      bus = holder.getBus();
      BusFactory.possiblySetDefaultBus(bus);
      //update the resource manager adding the ServletContextResourceResolver that was to be added by CXF servlet
      ResourceManager resourceManager = bus.getExtension(ResourceManager.class);
      resourceManager.addResourceResolver(new ServletContextResourceResolver(servletConfig.getServletContext()));
      replaceDestinationFactory();
      //set up the ServletController as the CXF servlet would have done
      controller = createServletController(servletConfig);
      //set the controller in the servlet context now that the bus has been configured in the servlet
      servletConfig.getServletContext().setAttribute(ServletController.class.getName(), getController());
   }

   private void initEndpoint(ServletConfig servletConfig)
   {
      SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
      epRegistry = spiProvider.getSPI(EndpointRegistryFactory.class).getEndpointRegistry();

      ServletContext context = servletConfig.getServletContext();
      String contextPath = context.getContextPath();
      endpoint = initServiceEndpoint(contextPath);
   }

   @Override
   protected void invoke(HttpServletRequest req, HttpServletResponse res) throws ServletException
   {
      try
      {
         BusFactory.setThreadDefaultBus(getBus());
         EndpointAssociation.setEndpoint(endpoint);
         RequestHandler requestHandler = (RequestHandler)endpoint.getRequestHandler();
         requestHandler.handleHttpRequest(endpoint, req, res, getServletContext());
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

   /** Initialize the service endpoint
    */
   private Endpoint initServiceEndpoint(String contextPath)
   {
      if (contextPath.startsWith("/"))
         contextPath = contextPath.substring(1);

      Endpoint endpoint = null;
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

      return endpoint;
   }

   private void registerInstrumentManger(Bus bus) throws ServletException
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
         Method method = CounterRepository.class.getDeclaredMethod("registerInterceptorsToBus", new Class[] {});
         method.setAccessible(true);
         method.invoke(couterRepository, new Object[] {});
      }
      catch (Exception e)
      {
         throw new ServletException(e);
      }
   }
}
