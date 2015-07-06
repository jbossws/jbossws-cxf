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
package org.jboss.wsf.stack.cxf.interceptor.util;

import java.util.List;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.message.Message;

/**
 * Abstract feature to initialize and remove the added interceptors by cxf's AbstractFeature
 * @author <a href="mailto:ema@redhat.com">Jim Ma</a>
 *
 */
public abstract class RemovableFeature
{
   private AbstractFeature cxfFeature;

   public RemovableFeature(AbstractFeature feature)
   {
      cxfFeature = feature;
   }

   public void initialize(Endpoint endpoint, Bus bus)
   {
      cxfFeature.initialize(endpoint, bus);
   }

   public abstract void remove(Endpoint endpoint);
   
   
   public void removeInterceptor(final List<Interceptor<? extends Message>> interceptors, final Class<? extends Interceptor<? extends Message>> interceptorClass) {
      for (int i = 0; i < interceptors.size(); i++)
      {
         if (interceptorClass.isInstance(interceptors.get(i)))
         {
            interceptors.remove(i);
         }
      }
   }

}
