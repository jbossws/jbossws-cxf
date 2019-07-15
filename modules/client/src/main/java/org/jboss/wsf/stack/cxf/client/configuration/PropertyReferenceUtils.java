/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2019, Red Hat Middleware LLC, and individual contributors
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
