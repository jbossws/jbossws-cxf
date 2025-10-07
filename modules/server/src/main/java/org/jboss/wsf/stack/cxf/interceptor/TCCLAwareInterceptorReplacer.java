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

import java.util.function.UnaryOperator;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.SoapInterceptor;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.jaxws.handler.soap.SOAPHandlerInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptor;

/**
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class TCCLAwareInterceptorReplacer implements UnaryOperator<Interceptor<? extends Message>> {

    public static final UnaryOperator<Interceptor<? extends Message>> INSTANCE = new TCCLAwareInterceptorReplacer();

    private TCCLAwareInterceptorReplacer() {
        // private constructor
    }

    @Override
    public Interceptor<? extends Message> apply(final Interceptor<? extends Message> interceptor) {
        return shouldReplaceInterceptor(interceptor) ? replace((PhaseInterceptor<? extends Message>)interceptor) : interceptor;
    }

    @SuppressWarnings("unchecked")
    private PhaseInterceptor<? extends Message> replace(final PhaseInterceptor<? extends Message> interceptor) {
        if (interceptor instanceof AbstractTCCLAwarePhaseInterceptor) {
            return interceptor; // nothing to replace - TCCL aware interceptor is already in place
        }
        if (interceptor instanceof SoapInterceptor) {
            return new TCCLAwareSoapPhaseInterceptor((PhaseInterceptor<SoapMessage>) interceptor);
        } else {
            return new TCCLAwarePhaseInterceptor((PhaseInterceptor<Message>) interceptor);
        }
    }

    private static boolean shouldReplaceInterceptor(final Interceptor<? extends Message> interceptor) {
        final boolean isApacheCXFSOAPHandlerInterceptor = interceptor instanceof SOAPHandlerInterceptor;
        final boolean isNotApacheCXFInterceptor = !interceptor.getClass().getName().startsWith("org.apache.cxf");
        return isApacheCXFSOAPHandlerInterceptor || isNotApacheCXFInterceptor;
    }

}
