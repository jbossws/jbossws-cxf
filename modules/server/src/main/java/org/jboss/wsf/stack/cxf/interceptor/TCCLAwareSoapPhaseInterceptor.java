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
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.soap.SOAPHandler;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.SoapInterceptor;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.phase.PhaseInterceptor;

import org.jboss.ws.common.utils.DelegateClassLoader;

/**
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
final class TCCLAwareSoapPhaseInterceptor extends AbstractTCCLAwarePhaseInterceptor<SoapMessage> implements SoapInterceptor {

    TCCLAwareSoapPhaseInterceptor(final PhaseInterceptor<SoapMessage> delegate) {
        super(delegate);
    }

    // TCCL aware methods

    @Override
    public Set<QName> getUnderstoodHeaders() {
        final ClassLoader original = SecurityActions.getContextClassLoader();
        try {
            if (original instanceof DelegateClassLoader) {
                DelegateClassLoader delegateCL = (DelegateClassLoader)original;
                SecurityActions.setContextClassLoader(delegateCL.getDelegate());
            }
            return ((SoapInterceptor) delegate).getUnderstoodHeaders();
        } finally {
            SecurityActions.setContextClassLoader(original);
        }
    }

    // TCCL unaware methods

    public Set<URI> getRoles() {
        return ((SoapInterceptor) delegate).getRoles();
    }

}
