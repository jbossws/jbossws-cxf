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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jboss.wsf.stack.cxf.client.Constants;
import org.jboss.wsf.stack.cxf.i18n.Loggers;

public final class PropertyReferenceUtils
{
   public static void createPropertyReference(Map<String, String> props, Map<String, Object> target) {
      List<String> propRefKeys = props.keySet().stream().filter(k -> {
         //filter out cxf intercetpors and features property which already processed
            if (k.startsWith("cxf.interceptors") || k.equalsIgnoreCase(Constants.CXF_FEATURES_PROP))
            {
               return false;

            }
            //ony get the value start with ## which is a reference
            if (props.get(k) != null && props.get(k).startsWith(MapToBeanConverter.BEAN_ID_PREFIX))
            {
               return true;
            }
            return false;
         }).collect(Collectors.toList());
      if (!propRefKeys.isEmpty())
      {
         MapToBeanConverter converter = new MapToBeanConverter(props);
         propRefKeys.forEach(key -> {
            try
            {
               target.put(key, converter.get(props.get(key)));
            }
            catch (Exception e)
            { 
               Loggers.DEPLOYMENT_LOGGER.unableToCreateConfigRef(key, e);
            }
         });
      }
   }
}
