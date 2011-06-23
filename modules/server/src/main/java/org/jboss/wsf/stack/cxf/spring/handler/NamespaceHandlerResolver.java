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
package org.jboss.wsf.stack.cxf.spring.handler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import org.jboss.logging.Logger;
import org.jboss.ws.api.util.BundleUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.xml.DefaultNamespaceHandlerResolver;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.ClassUtils;

/**
 * A custom namespace handler resolver that first try resolving using
 * the jbossws.spring.handlers configuration, eventually falling back
 * to the Spring default namespace handler resolver when nothing is
 * found.
 * 
 * @author alessio.soldano@jboss.com
 * @since 09-Apr-2010
 *
 */
public class NamespaceHandlerResolver extends DefaultNamespaceHandlerResolver
{
   private static final ResourceBundle bundle = BundleUtils.getBundle(NamespaceHandlerResolver.class);
   public static final String JBOSSWS_HANDLER_MAPPINGS_LOCATION = "META-INF/jbossws.spring.handlers";

   private static final Logger logger = Logger.getLogger(NamespaceHandlerResolver.class);

   private ClassLoader loader;

   private Map<Object, Object> jbosswsHandlerMappings;
   
   public NamespaceHandlerResolver()
   {
      super();
   }
   
   public NamespaceHandlerResolver(ClassLoader classLoader)
   {
      super(classLoader);
      this.loader = (classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader());
   }

   public NamespaceHandlerResolver(ClassLoader classLoader, String handlerMappingsLocation)
   {
      super(classLoader, handlerMappingsLocation);
      this.loader = (classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader());
   }

   public NamespaceHandler resolve(String namespaceUri)
   {
      NamespaceHandler jbosswsHandler = null;
      try
      {
         jbosswsHandler = resolveInternal(namespaceUri);
      }
      catch (Throwable t)
      {
         if (logger.isTraceEnabled())
            logger.trace("Unable to resolve JBossWS specific handler for namespace '" + namespaceUri
                  + "'; trying default namespace resolution...", t);
      }
      return jbosswsHandler != null ? jbosswsHandler : super.resolve(namespaceUri);
   }

   private NamespaceHandler resolveInternal(String namespaceUri)
   {
      Map<Object, Object> handlerMappings = getHandlerMappings();
      Object handlerOrClassName = handlerMappings.get(namespaceUri);
      if (handlerOrClassName == null)
      {
         return null;
      }
      else if (handlerOrClassName instanceof NamespaceHandler)
      {
         return (NamespaceHandler) handlerOrClassName;
      }
      else
      {
         String className = (String) handlerOrClassName;
         try
         {
            Class<?> handlerClass = ClassUtils.forName(className, this.loader);
            if (!NamespaceHandler.class.isAssignableFrom(handlerClass))
            {
               throw new FatalBeanException(BundleUtils.getMessage(bundle, "NOT_IMPLEMENT_NSHANDLER_INTERFACE", 
                     new Object[]{className, namespaceUri, NamespaceHandler.class.getName()}));
            }
            NamespaceHandler namespaceHandler = (NamespaceHandler) BeanUtils.instantiateClass(handlerClass);
            namespaceHandler.init();
            handlerMappings.put(namespaceUri, namespaceHandler);
            return namespaceHandler;
         }
         catch (ClassNotFoundException ex)
         {
            throw new FatalBeanException(BundleUtils.getMessage(bundle, "NSHANDLER_CLASS_NOT_FOUND", 
                  new Object[]{className, namespaceUri}), ex);
         }
         catch (LinkageError err)
         {
            throw new FatalBeanException(BundleUtils.getMessage(bundle, "INVALID_NAMESPACEHANDLER_CLASS",  
                  new Object[]{className, namespaceUri} ), err);
         }
      }
   }

   /**
    * Load the specified NamespaceHandler mappings lazily.
    */
   private Map<Object, Object> getHandlerMappings()
   {
      if (this.jbosswsHandlerMappings == null)
      {
         try
         {
            Properties mappings = PropertiesLoaderUtils.loadAllProperties(JBOSSWS_HANDLER_MAPPINGS_LOCATION,
                  this.loader);
            if (logger.isDebugEnabled())
            {
               logger.debug("Loaded mappings [" + mappings + "]");
            }
            this.jbosswsHandlerMappings = new HashMap<Object, Object>(mappings);
         }
         catch (IOException ex)
         {
            IllegalStateException ise = new IllegalStateException(
                  "Unable to load NamespaceHandler mappings from location [" + JBOSSWS_HANDLER_MAPPINGS_LOCATION + "]");
            ise.initCause(ex);
            throw ise;
         }
      }
      return this.jbosswsHandlerMappings;
   }
}
