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
import org.apache.cxf.interceptor.OneWayProcessorInterceptor;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.jboss.ws.api.util.ServiceLoader;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.spi.invocation.NamespaceContextSelectorWrapperFactory;

/**
 * An interceptor for storing the NamespaceContextSelector association into the Exchange
 * 
 * @author alessio.soldano@jboss.com
 * @since 03-Feb-2012
 *
 */
public class NsCtxSelectorStoreInterceptor extends AbstractPhaseInterceptor<Message>
{
   private final NamespaceContextSelectorWrapperFactory factory;
   
   public NsCtxSelectorStoreInterceptor()
   {
      super(Phase.PRE_LOGICAL);
      addBefore(OneWayProcessorInterceptor.class.getName());
      ClassLoader cl = ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader();
      factory = (NamespaceContextSelectorWrapperFactory) ServiceLoader.loadService(
            NamespaceContextSelectorWrapperFactory.class.getName(), null, cl);
   }
   
   @Override
   public void handleMessage(Message message) throws Fault
   {
      Exchange exchange = message.getExchange();
      if (exchange.isOneWay() && !isRequestor(message) && factory != null)
      {
         factory.getWrapper().storeCurrentThreadSelector(exchange);
      }
   }
}
