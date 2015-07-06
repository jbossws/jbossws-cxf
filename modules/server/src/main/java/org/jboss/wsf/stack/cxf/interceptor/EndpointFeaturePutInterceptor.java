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
package org.jboss.wsf.stack.cxf.interceptor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.cxf.binding.soap.interceptor.EndpointSelectionInterceptor;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.OutgoingChainInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.common.gzip.GZIPFeature;
import org.jboss.wsf.stack.cxf.interceptor.util.RemovableFeature;
import org.jboss.wsf.stack.cxf.interceptor.util.RemovableLoggingFeature;

/**
 * Interceptor to allow dynamically enalbe cxf features 
 * responds to url like http://localhost:8080/context/wsendpoint/management?logging=true
 * We now support Logging and GZIP feature
 * @author <a href="mailto:ema@redhat.com>Jim Ma</a>
 *
 */
public class EndpointFeaturePutInterceptor extends AbstractMangementInInterceptor
{
   public static final Set<String> httpMethods;
   public static final Map<String, RemovableFeature> featureClassMap = new HashMap<String, RemovableFeature>(4);
   private Interceptor<Message> configOutInteceptor = EndpointConfigsOutIntercetpor.INSTANCE;
   static
   {
      httpMethods = new HashSet<String>(4);
      httpMethods.add("PUT");
      httpMethods.add("GET");
      featureClassMap.put("logging", new RemovableLoggingFeature(new LoggingFeature()));
      featureClassMap.put("gzip", new RemovableLoggingFeature(new GZIPFeature()));
   }

   public EndpointFeaturePutInterceptor()
   {
      super(Phase.READ);
      getAfter().add(EndpointSelectionInterceptor.class.getName());
   }
   public EndpointFeaturePutInterceptor(Interceptor<Message> outInterceptor)
   {
      this();
      configOutInteceptor = outInterceptor;
   }

   @Override
   public void handleMessage(Message message) throws Fault
   {
      if (!isAllowed(message))
      {
         return;
      }
      if (isRecognizedQuery(getQueryMap(message)))
      {
         Map<String, String> queryMap = getQueryMap(message);
         for (Entry<String, String> entry : queryMap.entrySet())
         {
            if (entry.getValue().equals("true"))
            {
               featureClassMap.get(entry.getKey()).initialize(message.getExchange().getEndpoint(), message.getExchange().getBus());
            }
            else
            {
               featureClassMap.get(entry.getKey()).remove(message.getExchange().getEndpoint());
            }
         }
         Message mout = this.createOutMessage(message);
         mout.put(EndpointConfigsOutIntercetpor.CONFIG_RESULT, "Successfully set feature to endpoint");
         cleanUpOutInterceptors(mout);
         mout.getInterceptorChain().add(configOutInteceptor);
         message.getInterceptorChain().doInterceptStartingAt(message, OutgoingChainInterceptor.class.getName());
      }
      
   }

   @Override
   protected Set<String> getAllowedMethod()
   {
      return httpMethods;
   }

   private boolean isRecognizedQuery(Map<String, String> map)
   {
      if (map.isEmpty())
      {
         return false;
      }
      for (String key : map.keySet())
      {
         if (!featureClassMap.containsKey(key) || !("true").toLowerCase().equals(map.get(key)) && !("false").toLowerCase().equals(map.get(key)))
         {
            return false;
         }
      }
      return true;
   }
}
