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
package org.jboss.wsf.stack.cxf;

import org.jboss.wsf.framework.deployment.AbstractAspectizedEndpointServlet;
import org.jboss.wsf.spi.management.EndpointResolver;

/**
 * An aspectized CXF endpoint servlet that is installed for every web service endpoint on AS 5.x series
 * @author richard.opalka@jboss.com
 */
public final class AspectizedEndpointServlet extends AbstractAspectizedEndpointServlet
{

   /**
    * Provides CXF specific endpoint resolver
    * @param servletContext servlet context
    * @param servletName servlet name
    * @return new CXF specific endpoint resolver
    */
   @Override
   protected final EndpointResolver newEndpointResolver(String servletContext, String servletName)
   {
      return new WebAppResolver(servletContext, servletName);
   }

}
