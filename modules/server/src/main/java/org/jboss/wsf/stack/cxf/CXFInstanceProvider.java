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
package org.jboss.wsf.stack.cxf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.xml.ws.handler.Handler;

import org.apache.cxf.jaxws.support.JaxWsEndpointImpl;
import org.jboss.ws.common.configuration.ConfigDelegateHandler;
import org.jboss.ws.common.deployment.ReferenceFactory;
import org.jboss.wsf.spi.deployment.InstanceProvider;
import org.jboss.wsf.spi.deployment.Reference;
import org.jboss.wsf.stack.cxf.i18n.Messages;

/**
 * CXF instance provider.
 * 
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 * @author <a href="mailto:alessio.soldano@jboss.com">Alessio Soldano</a>
 */
public final class CXFInstanceProvider implements InstanceProvider {

    private final Object serviceBean;
    private final org.apache.cxf.endpoint.Endpoint cxfEndpoint;
    private final Map<String, Reference> cache = new HashMap<String, Reference>(8);

    public CXFInstanceProvider(final Object serviceBean, final org.apache.cxf.endpoint.Endpoint cxfEndpoint) {
        this.serviceBean = serviceBean;
        this.cxfEndpoint = cxfEndpoint;
    }

    @SuppressWarnings("rawtypes")
    public synchronized Reference getInstance(final String className) {
        Reference instance = cache.get(className);
        if (instance == null) {
            if (className.equals(serviceBean.getClass().getName())) {
                cache.put(className, instance = ReferenceFactory.newUninitializedReference(serviceBean));
            }
            if (instance == null)
            {
               List<Handler> chain = ((JaxWsEndpointImpl) cxfEndpoint).getJaxwsBinding().getHandlerChain();
               if (chain != null)
               {
                  for (Handler handler : chain)
                  {
                     if (handler instanceof ConfigDelegateHandler)
                     {
                        handler = ((ConfigDelegateHandler)handler).getDelegate();
                     }
                     if (className.equals(handler.getClass().getName()))
                     {
                        cache.put(className, instance = ReferenceFactory.newUninitializedReference(handler));
                     }
                  }
               }
            }
        }
        if (instance == null)
            throw Messages.MESSAGES.cannotLoadClass(className);
        return instance;
    }

}
