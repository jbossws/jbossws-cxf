/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.BusApplicationContext;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.buslifecycle.BusLifeCycleListener;
import org.apache.cxf.buslifecycle.BusLifeCycleManager;
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.resource.ResourceManager;
import org.jboss.wsf.stack.cxf.Loggers;
import org.jboss.wsf.stack.cxf.client.injection.JBossWSResourceInjectionResolver;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

/**
 * A JBossWS version of @see{org.apache.cxf.bus.spring.SpringBusFactory} that
 * allows for loading a custom BusApplicationContext for integration reasons.
 * 
 * @author alessio.soldano@jboss.com
 * @since 25-May-2010
 *
 */
@Deprecated
public class JBossWSSpringBusFactory extends SpringBusFactory
{
   private boolean customContextProvided = false;

   public JBossWSSpringBusFactory()
   {
      super();
   }

   public JBossWSSpringBusFactory(ApplicationContext context)
   {
      super(context);
      this.customContextProvided = (context != null);
   }

   /**
    * This overrides the Apache CXF method to delegate to
    * @see{org.jboss.wsf.stack.cxf.client.configuration.JBossWSNonSpringBusFactory}
    * when there's no need for a Spring bus.
    */
   @Override
   public Bus createBus(String cfgFiles[], boolean includeDefaults)
   {
      try
      {
         String userCfgFile = SecurityActions.getSystemProperty(Configurer.USER_CFG_FILE_PROPERTY_NAME, null);
         String sysCfgFileUrl = SecurityActions.getSystemProperty(Configurer.USER_CFG_FILE_PROPERTY_URL, null);
         Resource r = BusApplicationContext.findResource(Configurer.DEFAULT_USER_CFG_FILE);
         if (!customContextProvided && userCfgFile == null && cfgFiles == null && sysCfgFileUrl == null
               && (r == null || !r.exists()) && includeDefaults)
         {
            return new JBossWSNonSpringBusFactory().createBus();
         }
         return finishCreatingBus(createApplicationContext(cfgFiles, includeDefaults));
      }
      catch (BeansException ex)
      {
         throw new RuntimeException(ex);
      }
   }

   @Override
   public Bus createBus(URL[] urls, boolean includeDefaults)
   {
      try
      {
         return finishCreatingBus(new BusApplicationContext(urls, includeDefaults, getApplicationContext()));
      }
      catch (BeansException ex)
      {
         throw new RuntimeException(ex);
      }
   }

   private Bus finishCreatingBus(BusApplicationContext bac)
   {
      final Bus bus = (Bus) bac.getBean(Bus.DEFAULT_BUS_ID);

      bus.setExtension(bac, BusApplicationContext.class);
      
      setConfigurer(bus);
      
      possiblySetDefaultBus(bus);

      initializeBus(bus);

      registerAppContextLifeCycleListener(bus, bac);
      return bus;
   }

   protected BusApplicationContext createApplicationContext(String cfgFiles[], boolean includeDefaults)
   {
      try
      {
         return new BusApplicationContext(cfgFiles, includeDefaults, getApplicationContext());
      }
      catch (BeansException ex)
      {
         ClassLoader contextLoader = SecurityActions.getContextClassLoader();
         if (contextLoader != BusApplicationContext.class.getClassLoader())
         {
            Loggers.ROOT_LOGGER.appContextCreationFailedWillTryWithNewTCCL(contextLoader, BusApplicationContext.class.getClassLoader(), ex);
            SecurityActions.setContextClassLoader(BusApplicationContext.class.getClassLoader());
            try
            {
               return new BusApplicationContext(cfgFiles, includeDefaults, getApplicationContext());
            }
            finally
            {
               SecurityActions.setContextClassLoader(contextLoader);
            }
         }
         else
         {
            throw ex;
         }
      }
   }
   
   private void setConfigurer(Bus bus)
   {
      JBossWSSpringConfigurer configurer = new JBossWSSpringConfigurer(bus.getExtension(Configurer.class));
      configurer.setCustomizer(new BeanCustomizer());
      bus.setExtension(configurer, Configurer.class);
   }
   
   protected void initializeBus(Bus bus) {
      super.initializeBus(bus);
      final ResourceManager resourceManager = bus.getExtension(ResourceManager.class);
      resourceManager.addResourceResolver(JBossWSResourceInjectionResolver.getInstance());
      SecurityProviderConfig.setup(bus);
   }

   void registerAppContextLifeCycleListener(final Bus bus, final BusApplicationContext bac)
   {
      BusLifeCycleManager lm = bus.getExtension(BusLifeCycleManager.class);
      if (null != lm)
      {
         lm.registerLifeCycleListener(new BusApplicationContextLifeCycleListener(bac));
      }
   }

   static class BusApplicationContextLifeCycleListener implements BusLifeCycleListener
   {
      private BusApplicationContext bac;

      BusApplicationContextLifeCycleListener(BusApplicationContext b)
      {
         bac = b;
      }

      public void initComplete()
      {
      }

      public void preShutdown()
      {
      }

      public void postShutdown()
      {
         bac.close();
      }

   }
}
