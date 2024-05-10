package org.jboss.test.ws.jaxws.cxf.throttling;

import java.util.Collections;
import java.util.List;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.throttling.ThrottleResponse;
import org.apache.cxf.throttling.ThrottlingManager;
import org.jboss.wsf.spi.deployment.Endpoint;

public class TestThrottlingManager extends ThrottleResponse implements ThrottlingManager {
    @Override
    public List<String> getDecisionPhases() {
        return Collections.singletonList(Phase.PRE_STREAM);
    }

    @Override
    public ThrottleResponse getThrottleResponse(String phase, Message m) {
        Endpoint endpoint = m.getExchange().get(Endpoint.class);
        if (endpoint != null) {
            if (endpoint.getEndpointMetrics().getFaultCount() >= 3) {
                this.setResponseCode(429);
                this.setDelay(200);
                return this;
            }
        }
        return null;
    }
}