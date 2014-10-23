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

import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.Binding;
import javax.xml.ws.handler.Handler;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.jboss.ws.common.configuration.ConfigDelegateHandler;
import org.jboss.wsf.spi.metadata.j2ee.serviceref.UnifiedHandlerMetaData.HandlerType;


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
            HandlerSorter hs = new HandlerSorter();
            if (hs.split(list)) {
               binding.setHandlerChain(hs.merge());
            }
            hs.cleanup();
         }
      }
   }

   /**
    * Utility class for efficiently sorting handlers so that PRE handlers always
    * come before user handlers and POST handlers always come after user handlers.
    * 
    * We need to know if the sorted list is actually different from the original
    * list, as if they are the same we don't want to spend time on setting a new
    * handler chain, which can be expensive. Moreover we're not really interested
    * in sorting the whole chain, we only care about the PRE/user/POST requirement
    * and are fine with the relative order of handlers of the same type.
    * 
    * So we process the list in two phases:
    * 
    * 1) split phase: the handler list is scanned (O(n)) to split it into PRE, user
    *                 and POST handler sublists. The relative order of handlers of
    *                 the same type is not changed.
    *                 While splitting, the procedure also figures out if the list
    *                 actually requires sorting, that is whether the PRE-user-POST
    *                 order requirement is not satisfied; this allows avoiding
    *                 useless ordering if the list is already fine (which is the
    *                 most common scenario). 
    * 2) merge phase: the sublists are merged together (O(n)) into an ordered
    *                 handlers list
    *                 
    * The overall time complexity is O(2n).
    *
    */
   @SuppressWarnings("rawtypes")
   private class HandlerSorter {
      private List<Handler> pre;
      private List<Handler> ep;
      private List<Handler> post;
      
      /**
       * Read the handlers list and figures out if it requires sorting.
       * 
       * @param handlers
       * @return
       */
      public boolean split(List<Handler> handlers) {
         boolean requiresSort = false;
         HandlerType lastHandlerType = null;
         for (Handler h : handlers) {
            if (h instanceof ConfigDelegateHandler) {
               if (((ConfigDelegateHandler)h).isPre()) {
                  add(h, HandlerType.PRE);
                  if (lastHandlerType != null && lastHandlerType != HandlerType.PRE) {
                     requiresSort = true;
                  }
                  lastHandlerType = HandlerType.PRE;
               } else {
                  add(h, HandlerType.POST);
                  lastHandlerType = HandlerType.POST;
               }
            } else {
               add(h, HandlerType.ENDPOINT);
               if (lastHandlerType != null && lastHandlerType == HandlerType.POST) {
                  requiresSort = true;
               }
               lastHandlerType = HandlerType.ENDPOINT;
            }
         }
         return requiresSort;
      }
      
      
      
      public List<Handler> merge() {
         List<Handler> l = new ArrayList<Handler>();
         if (pre != null) {
            l.addAll(pre);
         }
         l.addAll(ep);
         if (post != null) {
            l.addAll(post);
         }
         return l;
      }
      
      public void cleanup() {
         this.pre = null;
         this.post = null;
         this.ep = null;
      }
      
      private void add(Handler h, HandlerType t) {
         switch (t)
         {
            case PRE :
               if (pre == null) {
                  pre = new ArrayList<Handler>(4);
               }
               pre.add(h);
               break;

            case POST :
               if (post == null) {
                  post = new ArrayList<Handler>(4);
               }
               post.add(h);
               break;

            default :
               if (ep == null) {
                  ep = new ArrayList<Handler>();
               }
               ep.add(h);
               break;
         }
      }
   }
}
