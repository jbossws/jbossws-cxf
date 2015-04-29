/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
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
 