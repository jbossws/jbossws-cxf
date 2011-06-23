/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

import org.apache.cxf.resource.ResourceResolver;
import org.jboss.logging.Logger;
import org.jboss.ws.api.util.BundleUtils;

/**
 * A CXF resource resolver that uses the JBossWS spi resource resolver,
 * which in turn uses unified virtual files to access resources attached
 * to the deployment (via JBoss VFS).
 * 
 * @author alessio.soldano@jboss.com
 * @since 17-Nov-2009
 *
 */
public class JBossWSResourceResolver implements ResourceResolver
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(JBossWSResourceResolver.class);
   private static Logger log = Logger.getLogger(JBossWSResourceResolver.class);
   private org.jboss.wsf.spi.deployment.ResourceResolver resolver;
   
   public JBossWSResourceResolver(org.jboss.wsf.spi.deployment.ResourceResolver resolver)
   {
      this.resolver = resolver;
   }
   
   public InputStream getAsStream(String resourcePath)
   {
      URL url = resolve(resourcePath, URL.class);
      if (url != null)
      {
         try
         {
            return url.openStream();
         }
         catch (IOException ioe)
         {
            log.warn(BundleUtils.getMessage(bundle, "CANNOT_OPEN_STREAM_FOR_RESOURCE",  resourcePath));
         }
      }
      return null;
   }

   public <T> T resolve(String resourcePath, Class<T> resourceType)
   {
      URL url = null;
      try
      {
         url = resolver.resolve(resourcePath);
      }
      catch (IOException ioe)
      {
        log.warn(BundleUtils.getMessage(bundle, "CANNOT_RESOLVE_RESOURCE",  resourcePath));
      }
      if (url != null && resourceType.isInstance(url))
      {
         return resourceType.cast(url);
      }
      return null;
   }

}
