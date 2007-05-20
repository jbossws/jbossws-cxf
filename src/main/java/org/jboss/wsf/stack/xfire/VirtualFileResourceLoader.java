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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.jboss.virtual.VirtualFile;

import com.sun.xml.ws.transport.http.ResourceLoader;

/**
 * A ResourceLoader that delegates to the VFS 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 25-Apr-2007
 */
public class VirtualFileResourceLoader implements ResourceLoader
{
   private final VirtualFile vfRoot;

   public VirtualFileResourceLoader(VirtualFile vfRoot)
   {
      this.vfRoot = vfRoot;
   }

   public URL getResource(String path) throws MalformedURLException
   {
      if (path.startsWith("/"))
         path = path.substring(1);
      
      URL resURL = null;
      try
      {
         VirtualFile vfChild = vfRoot.findChild(path);
         resURL = vfChild.toURL();
      }
      catch (Exception ex)
      {
         // ignore
      }
      return resURL;
   }

   public URL getCatalogFile() throws MalformedURLException
   {
      return getResource("/WEB-INF/jax-ws-catalog.xml");
   }

   public Set<String> getResourcePaths(String path)
   {
      if (path.startsWith("/"))
         path = path.substring(1);
      
      Set<String> paths = new HashSet<String>();
      try
      {
         VirtualFile vfChild = vfRoot.findChild(path);
         for (VirtualFile vf : vfChild.getChildren())
         {
            String name = vf.getName();
            paths.add(name);
         }
      }
      catch (Exception ex)
      {
         // ignore
      }
      return paths;
   }
}
