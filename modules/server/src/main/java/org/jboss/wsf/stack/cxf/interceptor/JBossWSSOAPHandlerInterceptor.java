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

import jakarta.xml.ws.Binding;
import jakarta.xml.ws.handler.Handler;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.jaxws.handler.soap.SOAPHandlerInterceptor;
import org.jboss.wsf.stack.cxf.JAXPDelegateClassLoader;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Set;
/**
 * SOAPHandlerInterceptor uses correct Thread Context Class Loader
 * to getHeaders() from {@code SOAPHandler}
 * This customized JBossWSSOAPHandlerInterceptor is to fix JBWS-4444
 * @author ema@redhat.com
 */
public class JBossWSSOAPHandlerInterceptor extends SOAPHandlerInterceptor {
    public JBossWSSOAPHandlerInterceptor(Binding binding) {
        super(binding);
    }

    @Override
    public Set<QName> getUnderstoodHeaders() {
        Set<QName> understood = new HashSet<>();
        for (Handler<?> h : getBinding().getHandlerChain()) {
            if (h instanceof SOAPHandler) {
                ClassLoader original = SecurityActions.getContextClassLoader();
                try {
                    if (original instanceof JAXPDelegateClassLoader) {
                        JAXPDelegateClassLoader jaxpLoader = (JAXPDelegateClassLoader)original;
                        SecurityActions.setContextClassLoader(jaxpLoader.getDelegate());
                    }
                    Set<QName> headers = CastUtils.cast(((SOAPHandler<?>) h).getHeaders());
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

}
