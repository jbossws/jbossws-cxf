/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.wsf.stack.cxf.resolver;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.cxf.resource.ResourceResolver;
import org.jboss.wsf.stack.cxf.i18n.Loggers;

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
   private final org.jboss.wsf.spi.deployment.ResourceResolver resolver;
   
   public JBossWSResourceResolver(org.jboss.wsf.spi.deployment.ResourceResolver resolver)
   {
      this.resolver = resolver;
      if (Loggers.ROOT_LOGGER.isDebugEnabled()) {
         Loggers.ROOT_LOGGER.debug("JBossWSResourceResolver created with underlying resolver: "
            + (resolver != null ? resolver.getClass().getName() : "null"));
      }
   }
   
   public InputStream getAsStream(String resourcePath)
   {
      if (Loggers.ROOT_LOGGER.isDebugEnabled()) {
         Loggers.ROOT_LOGGER.debug("getAsStream called with resourcePath: " + resourcePath);
      }
      URL url = resolve(resourcePath, URL.class);
      if (url != null)
      {
         if (Loggers.ROOT_LOGGER.isDebugEnabled()) {
            Loggers.ROOT_LOGGER.debug("Resolved resourcePath '" + resourcePath + "' to URL: " + url);
         }
         try
         {
            return url.openStream();
         }
         catch (IOException ioe)
         {
            Loggers.ROOT_LOGGER.cannotOpenStream(JBossWSResourceResolver.class.getSimpleName(), resourcePath);
         }
      }
      else if (Loggers.ROOT_LOGGER.isDebugEnabled()) {
         Loggers.ROOT_LOGGER.debug("getAsStream returning null for resourcePath: " + resourcePath);
      }
      return null;
   }

   public <T> T resolve(String resourcePath, Class<T> resourceType)
   {
      if (Loggers.ROOT_LOGGER.isDebugEnabled()) {
         Loggers.ROOT_LOGGER.debug("resolve called with resourcePath: '" + resourcePath
            + "', resourceType: " + resourceType.getName());
      }
      URL url = resolver.resolveFailSafe(resourcePath);
      if (Loggers.ROOT_LOGGER.isDebugEnabled()) {
         Loggers.ROOT_LOGGER.debug("Underlying SPI resolver.resolveFailSafe('" + resourcePath
            + "') returned: " + url);
      }
      if (url == null && Loggers.ROOT_LOGGER.isDebugEnabled()) {
         Loggers.ROOT_LOGGER.cannotResolveResource(JBossWSResourceResolver.class.getSimpleName(), resourcePath);
         // Add stack trace to see who's calling
         Loggers.ROOT_LOGGER.debug("Stack trace for failed resolution:", new Exception("Stack trace"));
      }
      if (url != null && resourceType.isInstance(url))
      {
         if (Loggers.ROOT_LOGGER.isDebugEnabled()) {
            Loggers.ROOT_LOGGER.debug("Returning resolved URL: " + url + " (type matches)");
         }
         return resourceType.cast(url);
      }
      if (Loggers.ROOT_LOGGER.isDebugEnabled()) {
         if (url != null) {
            Loggers.ROOT_LOGGER.debug("URL resolved but type mismatch. URL class: "
               + url.getClass().getName() + ", requested type: " + resourceType.getName());
         }
         Loggers.ROOT_LOGGER.debug("resolve returning null for resourcePath: " + resourcePath);
      }
      return null;
   }

}
