/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.OneWayProcessorInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.ws.addressing.soap.MAPCodec;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.EndpointType;

/**
 * This interceptor adds the following context properties to cxf message:
 * <p>1.decoupled_fault_support<p>
 * It enables decoupled faultTo. This is an optional feature in cxf and we
 * need this to be default to make it same behavior with native stack.
 * <p>2.OneWayProcessorInterceptor.USE_ORIGINAL_THREAD<p>
 * This will force one way operation to use original thread for ejb webserivce endpoint
 * <p>to avoid authorization failure from ejb container 
 * @author <a href="mailto:ema@redhat.com">Jim Ma</a>
 */
public class MessagePropertySettingInterceptor extends AbstractPhaseInterceptor<Message>
{
   private EjbWSOneWayThreadInterceptor ejbOneWayInterceptor = new EjbWSOneWayThreadInterceptor();

   public MessagePropertySettingInterceptor()
   {
      super(Phase.PRE_PROTOCOL);
      addBefore(MAPCodec.class.getName());
   }

   public void handleMessage(Message message)
   {
      message.put("org.apache.cxf.ws.addressing.decoupled_fault_support", true);
      message.getInterceptorChain().add(ejbOneWayInterceptor);
   }

   public void handleFault(Message message)
   {
      //complete
   }

   public class EjbWSOneWayThreadInterceptor extends AbstractPhaseInterceptor<Message>
   {
      public EjbWSOneWayThreadInterceptor()
      {
         super(Phase.PRE_LOGICAL);
         this.addBefore(OneWayProcessorInterceptor.class.getName());
      }

      @Override
      public void handleMessage(Message message) throws Fault
      {

         Endpoint endpoint = message.getExchange().get(Endpoint.class);
         if (endpoint == null)
         {
            return;
         }
         //Use original thread for oneway message to avoid authorization failure in ejb container for webservice endpoint 
         if (endpoint.getType() == EndpointType.JAXWS_EJB3 && message.getExchange().isOneWay() && !isRequestor(message))
         {
            message.put(OneWayProcessorInterceptor.USE_ORIGINAL_THREAD, true);
         }

      }
   }

}
