/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.bus.CXFBusFactory;
import org.apache.cxf.bus.extension.ExtensionManagerBus;
import org.apache.cxf.buslifecycle.BusLifeCycleListener;
import org.apache.cxf.buslifecycle.BusLifeCycleManager;
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.resource.ResourceManager;
import org.jboss.wsf.stack.cxf.client.ClientBusSelector;
import org.jboss.wsf.stack.cxf.client.ProviderImpl;
import org.jboss.wsf.stack.cxf.client.injection.JBossWSResourceInjectionResolver;

/**
 * JBossWS version of @see{org.apache.cxf.BusFactory}.
 * 
 * @author alessio.soldano@jboss.com
 * @since 16-Jun-2010
 *
 */
public class JBossWSBusFactory extends CXFBusFactory
{
   private static final Map<ClassLoader, Bus> classLoaderBusses = new WeakHashMap<ClassLoader, Bus>();
   
   @Override
   public Bus createBus(Map<Class<?>, Object> extensions, Map<String, Object> properties) {
      if (extensions == null)
      {
         extensions = new HashMap<Class<?>, Object>();
      }
      if (!extensions.containsKey(Configurer.class))
      {
         extensions.put(Configurer.class, new JBossWSNonSpringConfigurer(new BeanCustomizer()));
      }
      
      //Explicitly ask for the ProviderImpl.class.getClassLoader() to be used for getting
      //cxf bus extensions (as that classloader is the jaxws-client module one which 'sees' all
      //extensions, unless different dependencies are explicitly set)
      ExtensionManagerBus bus = new ExtensionManagerBus(extensions, properties, ProviderImpl.class.getClassLoader());
      
      possiblySetDefaultBus(bus);
      initializeBus(bus);
      bus.initialize();
      
      DefaultHTTPConduitFactoryWrapper.install(bus);
      return bus;
   }
   
   protected void initializeBus(Bus bus) {
      super.initializeBus(bus);
      final ResourceManager resourceManager = bus.getExtension(ResourceManager.class);
      resourceManager.addResourceResolver(JBossWSResourceInjectionResolver.getInstance());
      SecurityProviderConfig.setup(bus);
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
