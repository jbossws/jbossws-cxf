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
   private NamespaceContextSelectorWrapperFactory factory;
   
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
