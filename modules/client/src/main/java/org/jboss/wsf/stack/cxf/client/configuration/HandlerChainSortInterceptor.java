/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
      super(Phase.SETUP);
      binding = b;
   }

   @Override
   public void handleMessage(Message message) throws Fault
   {
      if (binding != null) {
         @SuppressWarnings("rawtypes")
         List<Handler> list = binding.getHandlerChain();
         if (list != null && !list.isEmpty()) {
            Collections.sort(list, comparator);
            binding.setHandlerChain(list);
         }
      }
   }

}
