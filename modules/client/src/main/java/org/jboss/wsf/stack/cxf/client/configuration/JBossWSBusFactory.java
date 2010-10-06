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
package org.jboss.wsf.stack.cxf.client.configuration;

import java.net.URL;
import java.util.Map;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.jboss.logging.Logger;
import org.jboss.wsf.stack.cxf.client.util.SpringUtils;

/**
 * JBossWS version of @see{org.apache.cxf.BusFactory}. This detects if Spring is available or not when the default
 * createBus() method is invoked; if Spring libraries are available in the classpath, an instance of
 * @see{org.jboss.wsf.stack.cxf.client.configuration.JBossWSSpringBusFactory} is internally used for
 * creating the bus. On the contrary, an instance of @see{org.jboss.wsf.stack.cxf.client.configuration.JBossWSNonSpringBusFactory}
 * is internally used when Spring is not available in the classpath.
 * Users willing to create a bus factory given a parent @see{org.springframework.context.ApplicationContext} should
 * directly create an instance of @see{org.jboss.wsf.stack.cxf.client.configuration.JBossWSSpringBusFactory}.
 * 
 * @author alessio.soldano@jboss.com
 * @since 16-Jun-2010
 *
 */
public class JBossWSBusFactory extends BusFactory
{
   private JBossWSSpringBusFactory springBusFactory;
   private JBossWSNonSpringBusFactory nonSpringBusFactory;
   
   @Override
   public Bus createBus()
   {
      if (SpringUtils.isSpringAvailable())
      {
         return getSpringBusFactory().createBus();
      }
      else
      {
         return getNonSpringBusFactory().createBus();
      }
   }
   
   /** JBossWSSpringBusFactory methods **/
   public Bus createBus(String cfgFile)
   {
      return getSpringBusFactory().createBus(cfgFile, true);
   }
   
   public Bus createBus(String cfgFiles[])
   {
      return getSpringBusFactory().createBus(cfgFiles, true);
   }
   
   public Bus createBus(String cfgFile, boolean includeDefaults)
   {
      return getSpringBusFactory().createBus(cfgFile, includeDefaults);
   }
   
   public Bus createBus(String cfgFiles[], boolean includeDefaults)
   {
      return getSpringBusFactory().createBus(cfgFiles, includeDefaults);
   }
   
   public Bus createBus(URL url)
   {
      return getSpringBusFactory().createBus(url);
   }

   public Bus createBus(URL[] urls)
   {
      return getSpringBusFactory().createBus(urls);
   }

   public Bus createBus(URL url, boolean includeDefaults)
   {
      return getSpringBusFactory().createBus(url, includeDefaults);
   }

   public Bus createBus(URL[] urls, boolean includeDefaults)
   {
      return getSpringBusFactory().createBus(urls, includeDefaults);
   }

   /** JBossWSNonSpringBusFactory methods **/
   @SuppressWarnings("rawtypes")
   public Bus createBus(Map<Class, Object> extensions)
   {
      return getNonSpringBusFactory().createBus(extensions);
   }

   @SuppressWarnings("rawtypes")
   public Bus createBus(Map<Class, Object> extensions, Map<String, Object> properties)
   {
      return getNonSpringBusFactory().createBus(extensions, properties);
   }

   /**
    * Makes sure the default bus is initialized
    */
   public static void initializeDefaultBus()
   {
      long i = System.currentTimeMillis();
      getDefaultBus();
      Logger.getLogger(JBossWSBusFactory.class).info("Time taken for initializeDefaultBus: " + (System.currentTimeMillis() - i));
   }
   
   public JBossWSSpringBusFactory getSpringBusFactory()
   {
      if (springBusFactory == null)
      {
         springBusFactory = new JBossWSSpringBusFactory();
      }
      return springBusFactory;
   }

   public JBossWSNonSpringBusFactory getNonSpringBusFactory()
   {
      if (nonSpringBusFactory == null)
      {
         nonSpringBusFactory = new JBossWSNonSpringBusFactory();
      }
      return nonSpringBusFactory;
   }
}
