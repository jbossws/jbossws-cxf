/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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
import java.util.ResourceBundle;

import javax.xml.ws.handler.Handler;

import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.jaxws.support.JaxWsEndpointImpl;
import org.jboss.ws.api.util.BundleUtils;
import org.jboss.ws.common.deployment.ReferenceFactory;
import org.jboss.wsf.spi.deployment.InstanceProvider;
import org.jboss.wsf.spi.deployment.Reference;

/**
 * CXF instance provider.
 * 
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class CXFInstanceProvider implements InstanceProvider {

    private static final ResourceBundle bundle = BundleUtils.getBundle(CXFInstanceProvider.class);
    private final ServerFactoryBean factory;
    private final Map<String, Reference> cache = new HashMap<String, Reference>();

    public CXFInstanceProvider(final ServerFactoryBean factory) {
        this.factory = factory;
    }

    public synchronized Reference getInstance(final String className) {
        Reference instance = cache.get(className);
        if (instance == null) {
            final Object serviceBean = factory.getServiceBean();
            if (className.equals(factory.getServiceBean().getClass().getName())) {
                cache.put(className, instance = ReferenceFactory.newUninitializedReference(serviceBean));
            }
            if (instance == null)
            {
                List<Handler> chain = ((JaxWsEndpointImpl) factory.getServer().getEndpoint()).getJaxwsBinding().getHandlerChain();
                if (chain != null) {
                    for (Handler handler : chain) {
                        if (className.equals(handler.getClass().getName())) {
                            cache.put(className, instance = ReferenceFactory.newUninitializedReference(handler));
                        }
                    }
                }
            }
        }
        if (instance == null)
            throw new IllegalStateException(BundleUtils.getMessage(bundle, "CANNOT_LOAD_CLASS",  className));
        return instance;
    }

}
