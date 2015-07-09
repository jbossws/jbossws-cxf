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
package org.jboss.wsf.stack.cxf.client.util;

/**
 * Utility class for SpringFramework related functions
 * 
 * @author alessio.soldano@jboss.com
 * @since 16-Jun-2010
 *
 */
@Deprecated
public class SpringUtils
{
   /**
    * Check if Spring is available using the provided classloader
    * 
    * @param loader
    * @return true if Spring libs are available
    */
   public static boolean isSpringAvailable(ClassLoader... loaders)
   {
      if (loaders == null || loaders.length == 0)
      {
         loaders = new ClassLoader[]{SecurityActions.getContextClassLoader()};
      }
      for (ClassLoader cl : loaders)
      {
         if (cl == null) 
         {
            continue;
         }
         try
         {
            cl.loadClass("org.springframework.context.ApplicationContext");
            return true;
         }
         catch (Exception e) {} //ignore
      }
      return false;
   }
}
