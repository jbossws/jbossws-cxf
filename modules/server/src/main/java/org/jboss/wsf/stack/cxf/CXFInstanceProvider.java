/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.wsf.stack.cxf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.ws.handler.Handler;

import org.apache.cxf.jaxws.support.JaxWsEndpointImpl;
import org.jboss.ws.common.configuration.ConfigDelegateHandler;
import org.jboss.ws.common.deployment.ReferenceFactory;
import org.jboss.wsf.spi.deployment.InstanceProvider;
import org.jboss.wsf.spi.deployment.Reference;

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
