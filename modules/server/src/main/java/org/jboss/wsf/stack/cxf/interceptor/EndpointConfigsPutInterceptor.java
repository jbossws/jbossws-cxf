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

/**
 * Interceptor to set runtime configuration with http put url like: http://localhost:8080/context/endpoint?statistics-enabled=true 
 *@author <a href="mailto:ema@redhat.com>Jim Ma</a>
 *
 */
public class EndpointConfigsPutInterceptor extends AbstractEndpintManagementInterceptor
{
   public static final EndpointConfigsPutInterceptor INSTANCE = new EndpointConfigsPutInterceptor();
   public static final String CONFIG_RESULT = EndpointConfigsPutInterceptor.class.getName() + ".EndpointConfigPutResult";
   public static final Set<String> httpMethods;
   private Interceptor<Message> configPutOutInteceptor = EndpointConfigsPutOutIntercetpor.INSTANCE;

   static
   {
      httpMethods = new HashSet<String>(4);
      httpMethods.add("PUT");
      httpMethods.add("GET");
   }

   public EndpointConfigsPutInterceptor()
   {
      super(Phase.READ);
      getAfter().add(EndpointSelectionInterceptor.class.getName());
   }

   public EndpointConfigsPutInterceptor(Interceptor<Message> outInterceptor)
   {
      this();
      configPutOutInteceptor = outInterceptor;
   }

   @Override
   public void handleMessage(Message message) throws Fault
   {
      if (!isAllowed(message))
      {
         return;
      }
      Map<String, String> queryMaps = getQueryMap(message);
      Endpoint endpoint = message.getExchange().get(Endpoint.class);
      for (String key : queryMaps.keySet())
      {
         if (!endpoint.getRuntimeConfigFlags().contains(key))
         {
            return;
         }
      }
      Message mout = this.createOutMessage(message);
      for (String key : queryMaps.keySet())
      {
         endpoint.setRuntimeProperty(key, queryMaps.get(key));
      }
      mout.put(CONFIG_RESULT, "Successfully set endpoint runtime configurations.");
      cleanUpOutInterceptors(mout);
      mout.getInterceptorChain().add(configPutOutInteceptor);
      message.getInterceptorChain().doInterceptStartingAt(message, OutgoingChainInterceptor.class.getName());
   }

   @Override
   Set<String> getAllowedMethod()
   {
      return httpMethods;
   }
}
