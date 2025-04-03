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

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import jakarta.xml.ws.Binding;
import jakarta.xml.ws.handler.Handler;
import jakarta.xml.ws.handler.soap.SOAPHandler;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.SoapInterceptor;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.phase.PhaseInterceptor;
import org.jboss.wsf.stack.cxf.JAXPDelegateClassLoader;

/**
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
final class TCCLAwareSoapPhaseInterceptor extends AbstractTCCLAwarePhaseInterceptor<SoapMessage> implements SoapInterceptor {

    private final Binding binding;

    TCCLAwareSoapPhaseInterceptor(final Binding binding, final PhaseInterceptor<SoapMessage> delegate) {
        super(delegate);
        this.binding = binding;
    }

    // TCCL aware methods

    @Override
    public Set<QName> getUnderstoodHeaders() {
        final Set<QName> understood = new HashSet<>();
        for (Handler<?> h : binding.getHandlerChain()) {
            if (h instanceof SOAPHandler) {
                final ClassLoader original = SecurityActions.getContextClassLoader();
                try {
                    if (original instanceof JAXPDelegateClassLoader) {
                        JAXPDelegateClassLoader jaxpLoader = (JAXPDelegateClassLoader)original;
                        SecurityActions.setContextClassLoader(jaxpLoader.getDelegate());
                    }
                    final Set<QName> headers = CastUtils.cast(((SOAPHandler<?>) h).getHeaders());
                    if (headers != null) {
                        understood.addAll(headers);
                    }
                } finally {
                    SecurityActions.setContextClassLoader(original);
                }
            }
        }
        return understood;
    }

    // TCCL unaware methods

    public Set<URI> getRoles() {
        return ((SoapInterceptor) delegate).getRoles();
    }

}
