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
package org.jboss.wsf.stack.cxf.deployment;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.cxf.jaxrs.lifecycle.ResourceProvider;
import org.apache.cxf.message.Message;


/**
 * 
 * 
 * @author alessio.soldano@jboss.com
 * @since 01-Feb-2016
 *
 */
public class JNDIComponentResourceProvider implements ResourceProvider
{
   private String jndiName;
   private InitialContext ctx;
   private volatile Object reference;
   private Class<?> scannable;
   private boolean cache;

   public JNDIComponentResourceProvider(String jndiName, Class<?> scannable, boolean cacheReference)
   {
      this.jndiName = jndiName;
      this.scannable = scannable;
      this.cache = cacheReference;
      try
      {
         ctx = new InitialContext();
      }
      catch (NamingException e)
      {
         throw new RuntimeException(e);
      }
   }

   @Override
   public Object getInstance(Message m)
   {
      if (reference != null) return reference;
      Object ref = reference;
      if (ref == null)
      {
         try
         {
            ref = ctx.lookup(jndiName);
         }
         catch (NamingException e)
         {
            throw new RuntimeException(e);
         }
         if (cache)
         {
            synchronized (this)
            {
               reference = ref;
            }
         }
      }
      return ref;
   }

   @Override
   public void releaseInstance(Message m, Object o)
   {
      //NOOP
   }

   @Override
   public Class<?> getResourceClass()
   {
      return scannable;
   }

   @Override
   public boolean isSingleton()
   {
      return false;
   }
   
}
