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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jakarta.xml.ws.Binding;
import jakarta.xml.ws.handler.Handler;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxws.handler.HandlerChainInvoker;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.jboss.ws.common.configuration.ConfigDelegateHandlerComparator;


/**
 * An interceptor for properly sorting handlers, some of which might come from PRE/POST
 * handler chains in the client / endpoint pre-defined configuration.
 * 
 * @author alessio.soldano@jboss.com
 * @since 06-Jun-2012
 */
public class HandlerChainSortInterceptor extends AbstractPhaseInterceptor<Message>
{
   private final Binding binding;
   @SuppressWarnings("rawtypes")
   private static final Comparator<Handler> comparator = new ConfigDelegateHandlerComparator<Handler>();

   public HandlerChainSortInterceptor(Binding b)
   {
      super(Phase.PRE_PROTOCOL);
      binding = b;
      //initially sort and reset the handler chain; if the chain is not modified later, the sort process
      //in handleMessage() deals with an already ordered list and is very efficient (~ O(n) according to
      //Collections.sort(..) javadoc.
      @SuppressWarnings("rawtypes")
      List<Handler> hc = binding.getHandlerChain();
      if (hc.size() > 1) { //no need to sort etc if the chain is empty or has one handler only
         Collections.sort(hc, comparator);
         binding.setHandlerChain(hc);
      }
   }

   @Override
   @SuppressWarnings("rawtypes")
   public void handleMessage(Message message) throws Fault
   {
      if (binding != null) {
         Exchange ex = message.getExchange();
         if (ex.get(HandlerChainInvoker.class) == null) {
            List<Handler> hc = binding.getHandlerChain();
            if (hc.size() > 1) { //no need to sort etc if the chain is empty or has one handler only
               Collections.sort(hc, comparator);
               //install a new HandlerChainInvoker using the sorted handler chain;
               //the AbstractJAXWSHandlerInterceptor will be using this invoker
               //instead of creating a new one
               ex.put(HandlerChainInvoker.class, new HandlerChainInvoker(hc, isOutbound(message, ex)));
            }
         }
      }
   }
   
   private boolean isOutbound(Message message, Exchange ex) {
      return message == ex.getOutMessage()
          || message == ex.getOutFaultMessage();
   }
   
}
