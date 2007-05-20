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

// $Id$

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.xml.ws.WebServiceException;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.server.BoundEndpoint;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.api.server.Module;
import com.sun.xml.ws.resources.WsservletMessages;
import com.sun.xml.ws.transport.http.DeploymentDescriptorParser.AdapterFactory;
import com.sun.xml.ws.transport.http.servlet.ServletAdapter;
import com.sun.xml.ws.transport.http.servlet.ServletAdapterList;
import com.sun.xml.ws.transport.http.servlet.WSServlet;
import com.sun.xml.ws.transport.http.servlet.WSServletDelegate;

/**
 * A copy of WSServletContextListener that externalizes
 * 
 * getDeploymentDescriptorURL()
 * createDeploymentDescriptorParser()
 *
 * @author WS Development Team
 * @author Thomas.Diesler@jboss.org
 * @since 10-May-2007
 */
public class WSServletContextListenerExt implements ServletContextAttributeListener, ServletContextListener
{
   private WSServletDelegate delegate;

   public void attributeAdded(ServletContextAttributeEvent event)
   {
   }

   public void attributeRemoved(ServletContextAttributeEvent event)
   {
   }

   public void attributeReplaced(ServletContextAttributeEvent event)
   {
   }

   public void contextDestroyed(ServletContextEvent event)
   {
      if (delegate != null)
      { // the deployment might have failed.
         delegate.destroy();
      }

      if (logger.isLoggable(Level.INFO))
      {
         logger.info(WsservletMessages.LISTENER_INFO_DESTROY());
      }
   }

   public void contextInitialized(ServletContextEvent event)
   {
      if (logger.isLoggable(Level.INFO))
      {
         logger.info(WsservletMessages.LISTENER_INFO_INITIALIZE());
      }
      ServletContext context = event.getServletContext();
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      if (classLoader == null)
      {
         classLoader = getClass().getClassLoader();
      }

      ServletContainer container = new ServletContainer(context);
      try
      {
         ServletResourceLoader resourceLoader = new ServletResourceLoader(context);
         ServletAdapterList adapterList = new ServletAdapterList();

         // Parse the descriptor file and build endpoint infos
         DeploymentDescriptorParserExt<ServletAdapter> parser = createDeploymentDescriptorParser(classLoader, container, resourceLoader, adapterList);

         URL sunJaxWsXml = getDeploymentDescriptorURL(context);
         List<ServletAdapter> adapters = parser.parse(sunJaxWsXml.toExternalForm(), sunJaxWsXml.openStream());

         delegate = new WSServletDelegate(adapters, context);

         context.setAttribute(WSServlet.JAXWS_RI_RUNTIME_INFO, delegate);
      }
      catch (Throwable e)
      {
         logger.log(Level.SEVERE, WsservletMessages.LISTENER_PARSING_FAILED(e), e);
         context.removeAttribute(WSServlet.JAXWS_RI_RUNTIME_INFO);
         throw new WebServiceException("listener.parsingFailed", e);
      }
   }

   /**
    * Externalized for integration 
    */
   protected DeploymentDescriptorParserExt<ServletAdapter> createDeploymentDescriptorParser(ClassLoader classLoader, ServletContainer container,
         ServletResourceLoader resourceLoader, AdapterFactory<ServletAdapter> adapterList) throws MalformedURLException
   {
      DeploymentDescriptorParserExt<ServletAdapter> parser = new DeploymentDescriptorParserExt<ServletAdapter>(classLoader, resourceLoader, container, adapterList);
      return parser;
   }

   /**
    * Externalized for integration 
    */
   protected URL getDeploymentDescriptorURL(ServletContext context) throws MalformedURLException
   {
      URL sunJaxWsXml = context.getResource(JAXWS_RI_RUNTIME);
      return sunJaxWsXml;
   }

   /**
    * Provides access to {@link ServletContext} via {@link Container}. Pipes
    * can get ServletContext from Container and use it to load some resources. 
    */
   static class ServletContainer extends Container
   {
      private final ServletContext servletContext;

      private final Module module = new Module()
      {
         private final List<BoundEndpoint> endpoints = new ArrayList<BoundEndpoint>();

         public @NotNull
         List<BoundEndpoint> getBoundEndpoints()
         {
            return endpoints;
         }
      };

      ServletContainer(ServletContext servletContext)
      {
         this.servletContext = servletContext;
      }

      public <T> T getSPI(Class<T> spiType)
      {
         if (spiType == ServletContext.class)
         {
            return (T)servletContext;
         }
         if (spiType == Module.class)
         {
            return spiType.cast(module);
         }
         return null;
      }
   }

   protected static final String JAXWS_RI_RUNTIME = "/WEB-INF/sun-jaxws.xml";

   private static final Logger logger = Logger.getLogger(com.sun.xml.ws.util.Constants.LoggingDomain + ".server.http");
}
