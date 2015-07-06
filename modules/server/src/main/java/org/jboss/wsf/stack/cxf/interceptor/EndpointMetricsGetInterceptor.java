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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.cxf.binding.soap.interceptor.EndpointSelectionInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.OutgoingChainInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.management.EndpointMetrics;

/**
 * Interceptor to get json format endpoint metrics. This interceptor is only 
 * responds to get url like http://localhost:8080/context/wsendpoint/management?metrics
 *@author <a href="mailto:ema@redhat.com>Jim Ma</a>
 *
 */
public class EndpointMetricsGetInterceptor extends AbstractMangementInInterceptor
{
   public static final EndpointMetricsGetInterceptor INSTANCE = new EndpointMetricsGetInterceptor();
   public static final String ENDPOINT_METRICS = EndpointMetricsGetInterceptor.class.getName() + ".EndpointMetrics";
   public static final Set<String> httpMethods = new HashSet<String>(4);
   private Interceptor<Message> metricsOutInteceptor = EndpointMetricsGetOutInterceptor.INSTANCE;
   private static final String TRANSFORM_SKIP = "transform.skip";
   static
   {
      httpMethods.add("GET");
   }

   public EndpointMetricsGetInterceptor()
   {
      super(Phase.READ);
      getAfter().add(EndpointSelectionInterceptor.class.getName());
   }

   public EndpointMetricsGetInterceptor(Interceptor<Message> outInterceptor)
   {
      this();
      metricsOutInteceptor = outInterceptor;
   }

   public void handleMessage(Message message) throws Fault
   {
      if (!isAllowed(message))
      {
         return;
      }
      if (isRecognizedQuery(getQueryMap(message)))
      {
         
         Endpoint endpoint = message.getExchange().get(Endpoint.class);
         EndpointMetrics endpointMetrics = endpoint.getEndpointMetrics();
         if (endpointMetrics == null) {
            return;
         }
         Message mout = this.createOutMessage(message);
         mout.put(ENDPOINT_METRICS, endpointMetrics);
         mout.put(Message.CONTENT_TYPE, "text/xml");
         // just remove the interceptor which should not be used
         cleanUpOutInterceptors(mout);
         // notice this is being added after the purge above, don't swap the order!

         mout.getInterceptorChain().add(metricsOutInteceptor);
         message.getExchange().put(TRANSFORM_SKIP, Boolean.TRUE);
         // skip the service executor and goto the end of the chain.
         message.getInterceptorChain().doInterceptStartingAt(message, OutgoingChainInterceptor.class.getName());
      }
   }

   private boolean isRecognizedQuery(Map<String, String> map)
   {
      if (map.containsKey("metrics") && map.size() == 1) {
         return true;
     }
     return false;
   }

   @Override
   Set<String> getAllowedMethod()
   {
      return httpMethods;
   }

}
