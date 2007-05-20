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
import java.util.Set;

import javax.servlet.ServletContext;

import com.sun.xml.ws.transport.http.ResourceLoader;

/**
 * {@link ResourceLoader} backed by {@link ServletContext}.
 *
 * TDI: A copy of the original that is public 
 *
 * @author WS Development Team
 * @author Thomas.Diesler@jboss.org
 */
public class ServletResourceLoader implements ResourceLoader
{
   private final ServletContext context;

   public ServletResourceLoader(ServletContext context)
   {
      this.context = context;
   }

   public URL getResource(String path) throws MalformedURLException
   {
      return context.getResource(path);
   }

   public URL getCatalogFile() throws MalformedURLException
   {
      return getResource("/WEB-INF/jax-ws-catalog.xml");
   }

   public Set<String> getResourcePaths(String path)
   {
      return context.getResourcePaths(path);
   }
}
