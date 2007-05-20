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

import javax.servlet.ServletContext;
import javax.xml.ws.WebServiceException;

import com.sun.xml.ws.resources.WsservletMessages;
import com.sun.xml.ws.transport.http.HttpAdapter;
import com.sun.xml.ws.transport.http.DeploymentDescriptorParser.AdapterFactory;
import com.sun.xml.ws.transport.http.servlet.ServletAdapter;

/**
 * Parses {@code sun-jaxws.xml} and sets up
 * {@link HttpAdapter}s for all deployed endpoints.
 *
 * <p>
 * This code is the entry point at the server side.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 18-May-2007
 */
public class WSServletContextListenerJBWS extends WSServletContextListenerExt
{
   public static final String PARAM_SUN_JAXWS_URL = "jbossws-sun-jaxws-url";

   /**
    * Fall back to the 'jbossws-sun-jaxws-url' init parameter for the URL location 
    * of sun-jaxws.xml  
    */
   @Override
   protected URL getDeploymentDescriptorURL(ServletContext context) throws MalformedURLException
   {
      URL sunJaxWsXml = super.getDeploymentDescriptorURL(context);
      if (sunJaxWsXml == null)
      {
         String urlStr = context.getInitParameter(PARAM_SUN_JAXWS_URL);
         if (urlStr != null && urlStr.length() > 0)
            sunJaxWsXml = new URL(urlStr);
      }
      if (sunJaxWsXml == null)
         throw new WebServiceException(WsservletMessages.NO_SUNJAXWS_XML(JAXWS_RI_RUNTIME));

      return sunJaxWsXml;
   }

   /**
    * Externalized for integration 
    */
   @Override
   protected DeploymentDescriptorParserExt<ServletAdapter> createDeploymentDescriptorParser(ClassLoader classLoader, ServletContainer container,
         ServletResourceLoader resourceLoader, AdapterFactory<ServletAdapter> adapterList) throws MalformedURLException
   {
      DeploymentDescriptorParserExt<ServletAdapter> parser = new DeploymentDescriptorParserJBWS<ServletAdapter>(classLoader, resourceLoader, container, adapterList);
      return parser;
   }
}
