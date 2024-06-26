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

import java.security.AccessController;
import java.util.Collections;
import java.util.List;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.throttling.ThrottleResponse;
import org.apache.cxf.throttling.ThrottlingManager;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.ws.common.management.AbstractServerConfig;
public class EndpointMetricsThrottlingManager extends ThrottleResponse implements ThrottlingManager {
    private long faultPermit = Integer.MAX_VALUE;
    private long requestPermit = Integer.MAX_VALUE;
    private long averageProcessingTimePermit = Long.MAX_VALUE;
    private long maxProcessingTimePermit = Long.MAX_VALUE;
    private long minProcessingTimePermit = Long.MAX_VALUE;

    private long totalProcessingTimePermit = Long.MAX_VALUE;

    private int responseStatusCode = 429;

    private long delayTime = 0;

    private String message = "";


    @Override
    public List<String> getDecisionPhases() {
        return Collections.singletonList(Phase.PRE_STREAM);
    }

    @Override
    public ThrottleResponse getThrottleResponse(String phase, Message m) {
        Endpoint endpoint = m.getExchange().get(Endpoint.class);
        if (endpoint != null && getServerConfig().isStatisticsEnabled()) {
            if (endpoint.getEndpointMetrics().getFaultCount() > this.getFaultPermit()
                    || endpoint.getEndpointMetrics().getRequestCount() > this.getRequestPermit()
                    || endpoint.getEndpointMetrics().getMaxProcessingTime() > this.getMaxProcessingTimePermit()
                    || endpoint.getEndpointMetrics().getMinProcessingTime() > this.getMinProcessingTimePermit()
                    || endpoint.getEndpointMetrics().getAverageProcessingTime() > this.getAverageProcessingTimePermit()
                    || endpoint.getEndpointMetrics().getTotalProcessingTime() > this.getTotalProcessingTimePermit()) {
                if (!getMessage().isEmpty()) {
                    this.setResponseCode(this.getResponseStatusCode(), this.getMessage());
                } else {
                    this.setResponseCode(this.getResponseStatusCode());
                }
                if (this.getDelayTime() > 0) {
                    this.setDelay(this.getDelayTime());
                }
                return this;
            }
        }
        return null;
    }

    public int getResponseStatusCode() {
        return responseStatusCode;
    }

    public void setResponseStatusCode(int responseStatusCode) {
        this.responseStatusCode = responseStatusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public long getFaultPermit() {
        return faultPermit;
    }

    public void setFaultPermit(long faultPermit) {
        this.faultPermit = faultPermit;
    }

    public long getRequestPermit() {
        return requestPermit;
    }

    public void setRequestPermit(long requestPermit) {
        this.requestPermit = requestPermit;
    }

    public long getMaxProcessingTimePermit() {
        return maxProcessingTimePermit;
    }

    public void setMaxProcessingTimePermit(long maxProcessingTimePermit) {
        this.maxProcessingTimePermit = maxProcessingTimePermit;
    }

    public long getMinProcessingTimePermit() {
        return minProcessingTimePermit;
    }

    public void setMinProcessingTimePermit(long minProcessingTimePermit) {
        this.minProcessingTimePermit = minProcessingTimePermit;
    }

    public long getTotalProcessingTimePermit() {
        return totalProcessingTimePermit;
    }

    public void setTotalProcessingTimePermit(long totalProcessingTimePermit) {
        this.totalProcessingTimePermit = totalProcessingTimePermit;
    }


    public long getAverageProcessingTimePermit() {
        return averageProcessingTimePermit;
    }

    public void setAverageProcessingTimePermit(long averageProcessingTimePermit) {
        this.averageProcessingTimePermit = averageProcessingTimePermit;
    }

    public long getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(long delayTime) {
        this.delayTime = delayTime;
    }

    private static ServerConfig getServerConfig() {
        if (System.getSecurityManager() == null) {
            return AbstractServerConfig.getServerIntegrationServerConfig();
        }
        return AccessController.doPrivileged(AbstractServerConfig.GET_SERVER_INTEGRATION_SERVER_CONFIG);
    }
}