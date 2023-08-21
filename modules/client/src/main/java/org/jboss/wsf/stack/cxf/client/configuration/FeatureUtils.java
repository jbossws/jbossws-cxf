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
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.cxf.Bus;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.interceptor.InterceptorProvider;
import org.jboss.wsf.stack.cxf.client.Constants;

/**
 * Utils methods for adding/removing CXF features
 * 
 * @author alessio.soldano@jboss.com
 * @since 10-Mar-2015
 *
 */
public class FeatureUtils
{
   public static void addFeatures(InterceptorProvider interceptorProvider, Bus bus, Map<String, String> properties) {
      final String features = properties.get(Constants.CXF_FEATURES_PROP);
      if (features != null) {
         MapToBeanConverter converter = new MapToBeanConverter(properties);
         for (Feature f : createFeatures(features, converter)) {
            f.initialize(interceptorProvider, bus);
         }
      }
   }
   
   private static List<Feature> createFeatures(String propValue, MapToBeanConverter converter) {
      List<Feature> list = new ArrayList<Feature>();
      StringTokenizer st = new StringTokenizer(propValue, ", ", false);
      
      while (st.hasMoreTokens()) {
         Feature feature = (Feature)newInstance(st.nextToken(), converter);
         if (feature != null) {
            list.add(feature);
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
