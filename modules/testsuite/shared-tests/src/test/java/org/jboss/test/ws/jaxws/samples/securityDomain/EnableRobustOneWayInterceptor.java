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