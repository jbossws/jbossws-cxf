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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.ws.Binding;
import javax.xml.ws.handler.Handler;

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
   private Binding binding;
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
