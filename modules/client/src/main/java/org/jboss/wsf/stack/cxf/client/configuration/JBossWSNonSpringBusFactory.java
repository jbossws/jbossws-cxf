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

import java.util.HashMap;
import java.util.Map;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.CXFBusFactory;
import org.apache.cxf.bus.extension.ExtensionManagerBus;
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.resource.ResourceManager;
import org.jboss.wsf.stack.cxf.client.ProviderImpl;
import org.jboss.wsf.stack.cxf.client.injection.JBossWSResourceInjectionResolver;

/**
 * 
 * @author alessio.soldano@jboss.com
 * @since 16-Jun-2010
 *
 */
public class JBossWSNonSpringBusFactory extends CXFBusFactory
{
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
}
