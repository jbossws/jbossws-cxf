/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
      MapToBeanConverter converter = null;
      final String inInterceptors = properties.get(Constants.CXF_IN_INTERCEPTORS_PROP);
      if (inInterceptors != null) {
         if (converter == null) {
            converter = new MapToBeanConverter(properties);
         }
         interceptorProvider.getInInterceptors().addAll(createInterceptors(inInterceptors, converter));
      }
      final String outInterceptors = properties.get(Constants.CXF_OUT_INTERCEPTORS_PROP);
      if (outInterceptors != null) {
         if (converter == null) {
            converter = new MapToBeanConverter(properties);
         }
         interceptorProvider.getOutInterceptors().addAll(createInterceptors(outInterceptors, converter));
      }
      final String inFaultInterceptors = properties.get(Constants.CXF_IN_FAULT_INTERCEPTORS_PROP);
      if (inFaultInterceptors != null) {
         if (converter == null) {
            converter = new MapToBeanConverter(properties);
         }
         interceptorProvider.getInFaultInterceptors().addAll(createInterceptors(inFaultInterceptors, converter));
      }
      final String outFaultInterceptors = properties.get(Constants.CXF_OUT_FAULT_INTERCEPTORS_PROP);
      if (outFaultInterceptors != null) {
         if (converter == null) {
            converter = new MapToBeanConverter(properties);
         }
         interceptorProvider.getOutFaultInterceptors().addAll(createInterceptors(outFaultInterceptors, converter));
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
   
   private static List<Interceptor<?>> createInterceptors(String propValue, MapToBeanConverter converter) {
      List<Interceptor<?>> list = new ArrayList<Interceptor<?>>();
      StringTokenizer st = new StringTokenizer(propValue, ", ", false );
      while (st.hasMoreTokens()) {
         Interceptor<?> interceptor = (Interceptor<?>)newInstance(st.nextToken(), converter);
         if (interceptor != null) {
            list.add(interceptor);
         }
      }
      return list;
   }
   
   private static Object newInstance(String className, MapToBeanConverter converter)
   {
      try
      {
         return className.startsWith(MapToBeanConverter.BEAN_ID_PREFIX) ? converter.get(className) : converter.newInstance(className);
      }
      catch (Exception e)
      {
         return null;
      }
   }
}
