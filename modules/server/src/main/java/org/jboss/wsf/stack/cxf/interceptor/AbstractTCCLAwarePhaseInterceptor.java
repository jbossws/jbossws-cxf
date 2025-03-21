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

import java.util.Collection;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.PhaseInterceptor;
import org.jboss.ws.common.utils.DelegateClassLoader;

/**
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
abstract class AbstractTCCLAwarePhaseInterceptor<M extends Message> extends AbstractPhaseInterceptor<M> {

    final PhaseInterceptor<M> delegate;

    AbstractTCCLAwarePhaseInterceptor(final PhaseInterceptor<M> delegate) {
        super(delegate.getId(), delegate.getPhase());
        this.delegate = delegate;
        this.addBefore(delegate.getBefore());
        this.addAfter(delegate.getAfter());
    }

    // TCCL aware methods

    @Override
    public void handleFault(final M message) {
        final ClassLoader original = SecurityActions.getContextClassLoader();
        try {
            if (original instanceof DelegateClassLoader) {
                DelegateClassLoader jaxpLoader = (DelegateClassLoader) original;
                SecurityActions.setContextClassLoader(jaxpLoader.getDelegate());
            }
            delegate.handleFault(message);
        } finally {
            SecurityActions.setContextClassLoader(original);
        }
    }

    @Override
    public void handleMessage(M message) throws Fault {
        final ClassLoader original = SecurityActions.getContextClassLoader();
        try {
            if (original instanceof DelegateClassLoader) {
                DelegateClassLoader jaxpLoader = (DelegateClassLoader) original;
                SecurityActions.setContextClassLoader(jaxpLoader.getDelegate());
            }
            delegate.handleMessage(message);
        } finally {
            SecurityActions.setContextClassLoader(original);
        }
    }

    // TCCL unaware methods

    @Override
    public Collection<PhaseInterceptor<? extends Message>> getAdditionalInterceptors() {
        return delegate.getAdditionalInterceptors();
    }

    // SoapInterceptor implementation methods - optional for this wrapper

    @Override
    public String toString() {
        return getClass().getName() + " --- delegating to ---> " + delegate.toString();
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof AbstractTCCLAwarePhaseInterceptor)) return false;
        return delegate.equals(((AbstractTCCLAwarePhaseInterceptor) obj).delegate);
    }
}
