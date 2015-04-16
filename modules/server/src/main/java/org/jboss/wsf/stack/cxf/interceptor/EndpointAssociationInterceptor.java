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
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.invocation.EndpointAssociation;

/**
 * A RECEIVE phase interceptor that sets the @see{org.jboss.wsf.spi.deployment.Endpoint}
 * associated to the current message exchange. This is performed early in the chain,
 * before any thread pool comes into the game, allowing the EndpointAssociation
 * threadlocal to retrieve the correct Endpoint instance.
 * 
 * @author alessio.soldano@jboss.com
 * @since 10-Jun-2010
 *
 */
public class EndpointAssociationInterceptor extends AbstractPhaseInterceptor<Message>
{

   
   public EndpointAssociationInterceptor()
   {
      super(Phase.RECEIVE);
   }

   @Override
   public void handleMessage(Message message) throws Fault
   {
      Endpoint endpoint = EndpointAssociation.getEndpoint();
      Exchange exchange = message.getExchange();
      exchange.put(Endpoint.class, endpoint);
   }
}
