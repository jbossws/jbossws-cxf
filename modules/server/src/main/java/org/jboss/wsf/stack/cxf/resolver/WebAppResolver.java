/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf.resolver;

import org.jboss.wsf.spi.management.EndpointResolver;
import org.jboss.wsf.spi.deployment.Endpoint;

import javax.management.ObjectName;
import java.util.Iterator;

/**
 * Resolves Endpoints by Servlet name and web context path.
 *
 * @author Heiko.Braun@jboss.com
 *         Created: Jul 24, 2007
 */
public class WebAppResolver implements EndpointResolver
{
   private String contextPath;
   private String servletName;

   public WebAppResolver(String contextPath, String servletName)
   {
      this.contextPath = contextPath;
      this.servletName = servletName;
   }

   public Endpoint query(Iterator<Endpoint> endpoints)
   {
      Endpoint endpoint = null;

      if (contextPath.startsWith("/"))
         contextPath = contextPath.substring(1);

      while(endpoints.hasNext())
      {
         Endpoint auxEndpoint = endpoints.next();
         ObjectName sepId = auxEndpoint.getName();
         String propContext = sepId.getKeyProperty(Endpoint.SEPID_PROPERTY_CONTEXT);
         String propEndpoint = sepId.getKeyProperty(Endpoint.SEPID_PROPERTY_ENDPOINT);
         if (servletName.equals(propEndpoint) && contextPath.equals(propContext))
         {
            endpoint = auxEndpoint;
            break;
         }
      }

      return endpoint;
   }
}
