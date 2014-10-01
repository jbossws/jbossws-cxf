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
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.buslifecycle.BusLifeCycleListener;
import org.apache.cxf.buslifecycle.BusLifeCycleManager;
import org.jboss.wsf.stack.cxf.client.ClientBusSelector;
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
   private static final Map<ClassLoader, Bus> classLoaderBusses = new WeakHashMap<ClassLoader, Bus>();
   
   private JBossWSSpringBusFactory springBusFactory;
   private JBossWSNonSpringBusFactory nonSpringBusFactory;
   
   @Override
   public Bus createBus()
   {
      if (isSpringAvailable())
      {
         return getSpringBusFactory().createBus();
      }
      else
      {
         return getNonSpringBusFactory().createBus();
      }
   }
   
   private boolean isSpringAvailable() {
      // Spring is available iff:
      // 1) TCCL has Spring classes
      // 2) the SpringBusFactory has already been loaded or the defining classloader can load that
      return (SpringUtils.isSpringAvailable() && (springBusFactory != null || SpringUtils.isSpringAvailable(this.getClass().getClassLoader())));
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
   public Bus createBus(Map<Class<?>, Object> extensions)
   {
      return getNonSpringBusFactory().createBus(extensions);
   }

   public Bus createBus(Map<Class<?>, Object> extensions, Map<String, Object> properties)
   {
      return getNonSpringBusFactory().createBus(extensions, properties);
   }
   
   public synchronized JBossWSSpringBusFactory getSpringBusFactory()
   {
      if (springBusFactory == null)
      {
         springBusFactory = new JBossWSSpringBusFactory();
      }
      return springBusFactory;
   }

   public synchronized JBossWSNonSpringBusFactory getNonSpringBusFactory()
   {
      if (nonSpringBusFactory == null)
      {
         nonSpringBusFactory = new JBossWSNonSpringBusFactory();
      }
      return nonSpringBusFactory;
   }
   
   /**
    * Gets (and internally sets) the default bus after having set the thread
    * context class loader to the provided one (which affects the Bus
    * construction if it's not been created yet). The former thread context
    * class loader is restored before returning to the caller.
    * 
    * @param contextClassLoader
    * @return the default bus
    */
   public static Bus getDefaultBus(ClassLoader contextClassLoader)
   {
      ClassLoader origClassLoader = SecurityActions.getContextClassLoader();
      try
      {
         SecurityActions.setContextClassLoader(contextClassLoader);
         return BusFactory.getDefaultBus();
      }
      finally
      {
         SecurityActions.setContextClassLoader(origClassLoader);
      }
   }
   
   /**
    * Gets the default bus for the given classloader; if a new Bus is needed,
    * the creation is delegated to the specified ClientBusSelector instance.
    * 
    * @param classloader
    * @param clientBusSelector
    * @return
    */
   public static Bus getClassLoaderDefaultBus(final ClassLoader classloader, final ClientBusSelector clientBusSelector) {
      Bus classLoaderBus;
      synchronized (classLoaderBusses) {
         classLoaderBus = classLoaderBusses.get(classloader);
         if (classLoaderBus == null) {
            classLoaderBus = clientBusSelector.createNewBus();
            //register a listener for cleaning up the bus from the classloader association in the JBossWSBusFactory
            BusLifeCycleListener listener = new ClassLoaderDefaultBusLifeCycleListener(classLoaderBus);
            classLoaderBus.getExtension(BusLifeCycleManager.class).registerLifeCycleListener(listener);
            classLoaderBusses.put(classloader, classLoaderBus);
         }
      }
      return classLoaderBus;
   }
   
   /**
    * Gets the default bus for the given classloader
    * 
    * @param classloader
    * @return
    */
   public static Bus getClassLoaderDefaultBus(final ClassLoader classloader) {
      Bus classLoaderBus;
      synchronized (classLoaderBusses) {
         classLoaderBus = classLoaderBusses.get(classloader);
         if (classLoaderBus == null) {
            classLoaderBus = new JBossWSBusFactory().createBus();
            //register a listener for cleaning up the bus from the classloader association in the JBossWSBusFactory
            BusLifeCycleListener listener = new ClassLoaderDefaultBusLifeCycleListener(classLoaderBus);
            classLoaderBus.getExtension(BusLifeCycleManager.class).registerLifeCycleListener(listener);
            classLoaderBusses.put(classloader, classLoaderBus);
         }
      }
      return classLoaderBus;
   }
   
   /**
    * Removes a bus from being the default bus for any classloader
    * 
    * @param bus
    */
   public static void clearDefaultBusForAnyClassLoader(final Bus bus) {
      synchronized (classLoaderBusses) {
         for (final Iterator<Bus> iterator = classLoaderBusses.values().iterator();
             iterator.hasNext();) {
             Bus itBus = iterator.next();
             if (bus == null || itBus == null|| bus.equals(itBus)) {
                 iterator.remove();
             }
         }
     }
   }
   
   private static class ClassLoaderDefaultBusLifeCycleListener implements BusLifeCycleListener {

      private final Bus bus;
      
      public ClassLoaderDefaultBusLifeCycleListener(final Bus bus) {
         this.bus = bus;
      }
      
      @Override
      public void initComplete()
      {
         // NOOP
      }

      @Override
      public void preShutdown()
      {
         // NOOP
      }

      @Override
      public void postShutdown()
      {
         JBossWSBusFactory.clearDefaultBusForAnyClassLoader(this.bus);
      }
   }
}
