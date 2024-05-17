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
package org.jboss.wsf.stack.cxf.features.throttling;

import org.apache.cxf.Bus;
import org.apache.cxf.feature.AbstractPortableFeature;
import org.apache.cxf.feature.DelegatingFeature;
import org.apache.cxf.interceptor.InterceptorProvider;
import org.apache.cxf.throttling.ThrottlingInterceptor;
import org.apache.cxf.throttling.ThrottlingManager;
import org.apache.cxf.throttling.ThrottlingResponseInterceptor;

public class JBossWSThrottlingFeature extends DelegatingFeature<JBossWSThrottlingFeature.Portable> {
    Portable portable = null;
    public JBossWSThrottlingFeature() {
        super(new JBossWSThrottlingFeature.Portable());
    }

    public void setThrottlingManager(ThrottlingManager manager) {
        this.getDelegate().setThrottlingManager(manager);
    }

    public static class Portable implements AbstractPortableFeature {

        ThrottlingManager manager = null;
        public Portable() {
        }

        public void setThrottlingManager(ThrottlingManager manager) {
            this.manager = manager;
        }

        @Override
        public void doInitializeProvider(InterceptorProvider provider, Bus bus) {
            ThrottlingManager m = manager;

            if (m == null) {
                throw new IllegalArgumentException("ThrottlingManager must not be null");
            }
            for (String p : m.getDecisionPhases()) {
                provider.getInInterceptors().add(new ThrottlingInterceptor(p, m));
            }
            provider.getOutInterceptors().add(new ThrottlingResponseInterceptor());
            provider.getOutFaultInterceptors().add(new ThrottlingResponseInterceptor());
        }
    }
}
