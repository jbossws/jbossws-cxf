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
