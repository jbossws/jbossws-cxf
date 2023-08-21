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
package org.jboss.test.ws.jaxws.samples.wsrm.store;

import java.util.Collection;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.ws.rm.RMDeliveryInterceptor;
import org.apache.cxf.ws.rm.RMManager;
import org.apache.cxf.ws.rm.SourceSequence;
/**
 * Interceptor to check if the RMStore is enabled and stores data
 * @author <a herf="mailto:ema@redhat.com">Jim Ma</a>
 *
 */
public class RMStoreCheckInterceptor extends AbstractPhaseInterceptor<Message>
{

   public static volatile int seqSize;
   private String endpointIdentifier = "{http://www.jboss.org/jbossws/ws-extensions/wsrm}RMService.{http://www.jboss.org/jbossws/ws-extensions/wsrm}RMEndpointPort@cxf";
   public RMStoreCheckInterceptor()
   {
      super(Phase.POST_INVOKE);
      this.addBefore(RMDeliveryInterceptor.class.getName());
   }

   @Override
   public void handleMessage(Message message) throws Fault
   {
      RMManager rmManager = message.getExchange().getBus().getExtension(RMManager.class);
      Collection<SourceSequence> seqs = rmManager.getStore().getSourceSequences(endpointIdentifier);
      if (seqs != null) {
         seqSize = seqs.size();
      }
   }

}
 