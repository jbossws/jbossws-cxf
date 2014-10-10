/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.InterceptorProvider;
import org.jboss.ws.common.utils.DelegateClassLoader;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.stack.cxf.client.Constants;

/**
 * Utils methods for adding/removing CXF interceptors
 * 
 * @author alessio.soldano@jboss.com
 * @since 10-Oct-2014
 *
 */
public class InterceptorUtils
{
   public static void addInterceptors(InterceptorProvider interceptorProvider, Map<String, String> properties) {
      final String inInterceptors = properties.get(Constants.CXF_IN_INTERCEPTORS_PROP);
      if (inInterceptors != null) {
         interceptorProvider.getInInterceptors().addAll(createInterceptors(inInterceptors));
      }
      final String outInterceptors = properties.get(Constants.CXF_OUT_INTERCEPTORS_PROP);
      if (outInterceptors != null) {
         interceptorProvider.getOutInterceptors().addAll(createInterceptors(outInterceptors));
      }
   }
   
   public static void removeInterceptors(List<Interceptor<?>> interceptorsList, String interceptors) {
      Set<String> set = new HashSet<String>();
      StringTokenizer st = new StringTokenizer(interceptors, ", ", false);
      while (st.hasMoreTokens()) {
         set.add(st.nextToken());
      }
      List<Interceptor<?>> toBeRemoved = new ArrayList<Interceptor<?>>();
      for (Interceptor<?> itc : interceptorsList) {
         if (set.contains(itc.getClass().getName())) {
            toBeRemoved.add(itc);
         }
      }
      interceptorsList.removeAll(toBeRemoved);
   }
   
   private static List<Interceptor<?>> createInterceptors(String propValue) {
      List<Interceptor<?>> list = new ArrayList<Interceptor<?>>();
      StringTokenizer st = new StringTokenizer(propValue, ", ", false );
      while (st.hasMoreTokens()) {
         String itc = st.nextToken();
         Interceptor<?> interceptor = (Interceptor<?>)newInstance(itc);
         if (interceptor != null) {
            list.add(interceptor);
         }
      }
      return list;
   }
   
   private static Object newInstance(String className)
   {
      try
      {
         ClassLoader loader = new DelegateClassLoader(ClassLoaderProvider.getDefaultProvider()
               .getServerIntegrationClassLoader(), SecurityActions.getContextClassLoader());
         Class<?> clazz = SecurityActions.loadClass(loader, className);
         return clazz.newInstance();
      }
      catch (Exception e)
      {
         return null;
      }
   }
}
