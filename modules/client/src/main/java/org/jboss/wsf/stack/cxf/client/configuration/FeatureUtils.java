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
