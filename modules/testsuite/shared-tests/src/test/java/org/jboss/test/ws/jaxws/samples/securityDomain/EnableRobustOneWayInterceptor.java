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
package org.jboss.test.ws.jaxws.samples.securityDomain;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.OneWayProcessorInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.EndpointType;

/**
 * This class for test use to enable client to receive the authorization
 * <p> exception for one way operation
 * @author <a href="mailto:ema@redhat.com">Jim Ma</a>
 *
 */
public class EnableRobustOneWayInterceptor extends AbstractPhaseInterceptor<Message>
{
   public EnableRobustOneWayInterceptor()
   {
      super(Phase.PRE_LOGICAL);
      this.addBefore(OneWayProcessorInterceptor.class.getName());
   }

   @Override
   public void handleMessage(Message message) throws Fault
   {

      Endpoint endpoint = message.getExchange().get(Endpoint.class);
      //Use original thread for oneway message to avoid authorization failure in ejb container for webservice endpoint 
      if (endpoint.getType() == EndpointType.JAXWS_EJB3 && message.getExchange().isOneWay() && !isRequestor(message))
      {
         message.put(Message.ROBUST_ONEWAY, true);
      }

   }
}