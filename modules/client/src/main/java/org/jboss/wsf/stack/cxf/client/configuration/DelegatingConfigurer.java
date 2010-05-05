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

import org.apache.cxf.configuration.Configurer;

/**
 * A CXF configurer that allows for custom configuration before delegating to another configurer;
 * this is installed by JBossWS' {@see org.jboss.wsf.stack.cxf.client.configuration.ConfigurerInstaller}
 * (see cxf-extension-jbossws.xml) to allow for custom client side configuration while leaving
 * Apache CXF the freedom of setting the initial configurer. 
 *
 * @author alessio.soldano@jboss.com
 * @since 04-May-2010
 */
public abstract class DelegatingConfigurer implements Configurer
{
   protected Configurer delegate;

   public DelegatingConfigurer(Configurer delegate)
   {
      this.delegate = delegate;
   }
   
   @Override
   public void configureBean(Object beanInstance)
   {
      internalConfigure(beanInstance);
      if (delegate != null)
      {
         delegate.configureBean(beanInstance);
      }
   }

   @Override
   public void configureBean(String name, Object beanInstance)
   {
      internalConfigure(beanInstance);
      if (delegate != null)
      {
         delegate.configureBean(name, beanInstance);
      }
   }
   
   /**
    * Performs custom configurations on the provided bean instance
    * before delegating to the other configurer.
    * 
    * @param beanInstance
    */
   protected abstract void internalConfigure(Object beanInstance);
   
}
