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
package org.jboss.wsf.stack.cxf.client.configuration;

import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.configuration.spring.ConfigurerImpl;
import org.apache.cxf.extension.BusExtension;
import org.jboss.wsf.stack.cxf.Messages;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * A CXF delegate configurer that sets JBossWS stuff / customizations / properties etc. in CXF configurable beans
 * (to be used for Spring based bus)
 *
 * @author alessio.soldano@jboss.com
 * @since 05-Oct-2009
 */
@Deprecated
public class JBossWSSpringConfigurer implements JBossWSConfigurer, ApplicationContextAware, BusExtension
{
   private BeanCustomizer customizer;
   private final Configurer delegate;
   
   public JBossWSSpringConfigurer(Configurer delegate)
   {
      this.delegate = delegate;
   }

   @Override
   public void configureBean(Object beanInstance)
   {
      customConfigure(beanInstance);
      delegate.configureBean(beanInstance);
   }

   @Override
   public void configureBean(String name, Object beanInstance)
   {
      customConfigure(beanInstance);
      delegate.configureBean(name, beanInstance);
   }
   
   public void addApplicationContext(ApplicationContext ctx)
   {
      if (delegate instanceof ConfigurerImpl)
      {
         ((ConfigurerImpl)delegate).addApplicationContext(ctx);
      }
   }
   
   protected synchronized void customConfigure(Object beanInstance)
   {
      if (customizer != null)
      {
         customizer.customize(beanInstance);
      }
   }

   public BeanCustomizer getCustomizer()
   {
      return customizer;
   }

   public void setCustomizer(BeanCustomizer customizer)
   {
      this.customizer = customizer;
   }

   @Override
   public Class<?> getRegistrationType()
   {
      if (delegate instanceof BusExtension)
      {
         return ((BusExtension)delegate).getRegistrationType();
      }
      throw Messages.MESSAGES.notABusExtensionInstance(delegate);
   }

   @Override
   public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
   {
      if (delegate instanceof ApplicationContextAware)
      {
         ((ApplicationContextAware)delegate).setApplicationContext(applicationContext);
      }
   }
}
