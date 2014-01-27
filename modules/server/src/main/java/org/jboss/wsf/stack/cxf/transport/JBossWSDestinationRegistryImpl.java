/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.cxf.transport.http.DestinationRegistryImpl;

/**
 * A JBossWS version of the CXF DestinationRegistryImpl that registers destinations
 * with different key values.
 *
 * @author alessio.soldano@jboss.com
 * @since 23-Jan-2014
 *
 */
public class JBossWSDestinationRegistryImpl extends DestinationRegistryImpl
{

   /**
    * Return a real path value, removing the protocol, host and port
    * if specified.
    * 
    * @param path 
    * @return trimmed path
    */
   @Override
   public String getTrimmedPath(String path)
   {
      if (path == null)
      {
         return "/";
      }
      if (!path.startsWith("/"))
      {
         try
         {
            path = new URL(path).getPath();
         }
         catch (MalformedURLException ex)
         {
            // ignore
         }
         if (!path.startsWith("/")) {
            path = "/" + path;
         }
      }
      return path;
   }
}
