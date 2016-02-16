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

import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

import org.apache.cxf.jaxrs.lifecycle.PerRequestResourceProvider;
import org.apache.cxf.message.Message;
/**
 * @author <a href="mailto:ema@redhat.com>Jim Ma</a>
 */
public class CDIResourceProvider<T> extends PerRequestResourceProvider
{
   private Class<?> clazz;
   private JaxRsCdiResourceLocator cdiInjector;

   public CDIResourceProvider(Class<?> clazz)
   {
      super(clazz);
      cdiInjector = new JaxRsCdiResourceLocator();
      this.clazz = clazz;
   }

   //resource is requestScoped
   public boolean isSingleton()
   {
      return false;
   }

   @SuppressWarnings("unchecked")
   protected T createInstance(Message m)
   {
      Set<Bean<?>> beans = cdiInjector.getManager().getBeans(clazz);     
      Bean<T> bean = (Bean<T>)cdiInjector.getManager().resolve(beans);
      if (bean != null)
      {
         CreationalContext<T> context = cdiInjector.getManager().createCreationalContext(bean);
         //return an instance which will be used to inject jaxrs property like @Conext by cxf
         return cdiInjector.getManager().getContext(bean.getScope()).get(bean, context);
      } else {
         return (T)super.getInstance(m);
      }
       
   }
}
