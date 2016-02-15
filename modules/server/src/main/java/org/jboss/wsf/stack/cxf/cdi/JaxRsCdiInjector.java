/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf.cdi;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @author <a href="mailto:ema@redhat.com>Jim Ma</a>
 */
public class JaxRsCdiInjector
{
   private volatile BeanManager manager = null;

   public JaxRsCdiInjector()
   {
   }

   public BeanManager getManager()
   {
      if (manager == null)
      {
         this.manager = this.lookupBeanManager();
      }
      return manager;
   }

   protected BeanManager lookupBeanManager()
   {
      BeanManager beanManager = null;

      // Do a lookup for BeanManager in JNDI (this is the only *portable* way)
      beanManager = lookupBeanManagerInJndi("java:comp/BeanManager");
      if (beanManager != null)
      {
         return beanManager;
      }

      // Do a lookup for BeanManager at an alternative JNDI location (workaround for WELDINT-19)
      beanManager = lookupBeanManagerInJndi("java:app/BeanManager");
      if (beanManager != null)
      {
         return beanManager;
      }
      try
      {
         beanManager = CDI.current().getBeanManager();
         if (beanManager != null)
         {
            return beanManager;
         }
      }
      catch (Exception e)
      {
         //TODO: Add logger
         e.printStackTrace();
         
      }
      return null;
   }

   private BeanManager lookupBeanManagerInJndi(String name)
   {
      try
      {
         InitialContext ctx = new InitialContext();
         return (BeanManager) ctx.lookup(name);
      }
      catch (NamingException e)
      {
         return null;
      }
      catch (NoClassDefFoundError ncdfe)
      {
         return null;
      }
   }
}
