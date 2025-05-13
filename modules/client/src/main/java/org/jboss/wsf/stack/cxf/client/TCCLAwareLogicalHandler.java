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
package org.jboss.wsf.stack.cxf.client;

import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.MessageContext;
import org.jboss.ws.common.utils.DelegateClassLoader;

/**
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
@SuppressWarnings({"rawtypes", "unchecked"})
final class TCCLAwareLogicalHandler implements LogicalHandler {

    private final LogicalHandler delegate;

    TCCLAwareLogicalHandler(final LogicalHandler delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean handleMessage(final MessageContext messageContext) {
        final ClassLoader original = SecurityActions.getContextClassLoader();
        try {
            if (original instanceof DelegateClassLoader) {
                DelegateClassLoader delegateCL = (DelegateClassLoader) original;
                SecurityActions.setContextClassLoader(delegateCL.getDelegate());
            }
            return delegate.handleMessage(messageContext);
        } finally {
            SecurityActions.setContextClassLoader(original);
        }
    }

    @Override
    public boolean handleFault(final MessageContext messageContext) {
        final ClassLoader original = SecurityActions.getContextClassLoader();
        try {
            if (original instanceof DelegateClassLoader) {
                DelegateClassLoader delegateCL = (DelegateClassLoader) original;
                SecurityActions.setContextClassLoader(delegateCL.getDelegate());
            }
            return delegate.handleFault(messageContext);
        } finally {
            SecurityActions.setContextClassLoader(original);
        }
    }

    @Override
    public void close(final MessageContext messageContext) {
        final ClassLoader original = SecurityActions.getContextClassLoader();
        try {
            if (original instanceof DelegateClassLoader) {
                DelegateClassLoader delegateCL = (DelegateClassLoader) original;
                SecurityActions.setContextClassLoader(delegateCL.getDelegate());
            }
            delegate.close(messageContext);
        } finally {
            SecurityActions.setContextClassLoader(original);
        }
    }

}
