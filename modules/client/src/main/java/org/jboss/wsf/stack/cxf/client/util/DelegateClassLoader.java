/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf.client.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * A delegate classloader
 * 
 * @author alessio.soldano@jboss.com
 *
 */
public class DelegateClassLoader extends SecureClassLoader
{
   private ClassLoader delegate;

   private ClassLoader parent;

   public DelegateClassLoader(final ClassLoader delegate, final ClassLoader parent)
   {
      super(parent);
      this.delegate = delegate;
      this.parent = parent;
   }

   /** {@inheritDoc} */
   @Override
   public Class<?> loadClass(final String className) throws ClassNotFoundException
   {
      if (parent != null)
      {
         try
         {
            return parent.loadClass(className);
         }
         catch (ClassNotFoundException cnfe)
         {
            //NOOP, use delegate
         }
      }
      return delegate.loadClass(className);
   }

   /** {@inheritDoc} */
   @Override
   public URL getResource(final String name)
   {
      URL url = null;
      if (parent != null)
      {
         url = parent.getResource(name);
      }
      return (url == null) ? delegate.getResource(name) : url;
   }

   /** {@inheritDoc} */
   @Override
   public Enumeration<URL> getResources(final String name) throws IOException
   {
      final ArrayList<Enumeration<URL>> foundResources = new ArrayList<Enumeration<URL>>();

      foundResources.add(delegate.getResources(name));
      if (parent != null)
      {
         foundResources.add(parent.getResources(name));
      }

      return new Enumeration<URL>()
      {
         private int position = foundResources.size() - 1;

         public boolean hasMoreElements()
         {
            while (position >= 0)
            {
               if (foundResources.get(position).hasMoreElements())
               {
                  return true;
               }
               position--;
            }
            return false;
         }

         public URL nextElement()
         {
            while (position >= 0)
            {
               try
               {
                  return (foundResources.get(position)).nextElement();
               }
               catch (NoSuchElementException e)
               {
               }
               position--;
            }
            throw new NoSuchElementException();
         }
      };
   }

   /** {@inheritDoc} */
   @Override
   public InputStream getResourceAsStream(final String name)
   {
      URL foundResource = getResource(name);
      if (foundResource != null)
      {
         try
         {
            return foundResource.openStream();
         }
         catch (IOException e)
         {
         }
      }
      return null;
   }
}