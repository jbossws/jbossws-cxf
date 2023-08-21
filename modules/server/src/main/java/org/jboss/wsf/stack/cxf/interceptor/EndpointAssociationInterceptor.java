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
