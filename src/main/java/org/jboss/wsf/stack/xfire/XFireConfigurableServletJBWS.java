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

import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.codehaus.xfire.XFire;
import org.codehaus.xfire.transport.http.XFireConfigurableServlet;

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
}
