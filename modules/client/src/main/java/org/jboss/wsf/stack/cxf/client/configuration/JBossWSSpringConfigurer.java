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

/**
 * A CXF delegate configurer that sets JBossWS stuff / customizations / properties etc. in CXF configurable beans
 * (to be used for Spring based bus)
 *
 * @author alessio.soldano@jboss.com
 * @since 05-Oct-2009
 */
public class JBossWSSpringConfigurer implements JBossWSConfigurer
{
   private BeanCustomizer customizer;
   private Configurer delegate;
   
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
}
