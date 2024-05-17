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
    private long faultCountThreshold = Integer.MAX_VALUE;
    private long requestCountThreshold = Integer.MAX_VALUE;
    private long averageProcessingTimeThreshold = Long.MAX_VALUE;
    private long maxProcessingTimeThreshold = Long.MAX_VALUE;
    private long minProcessingTimeThreshold = Long.MAX_VALUE;

    private long totalProcessingTimeThreshold = Long.MAX_VALUE;

    private int responseStausCode = 429;

    private long delayTime = 200;

    private String message = "";


    @Override
    public List<String> getDecisionPhases() {
        return Collections.singletonList(Phase.PRE_STREAM);
    }

    @Override
    public ThrottleResponse getThrottleResponse(String phase, Message m) {
        Endpoint endpoint = m.getExchange().get(Endpoint.class);
        if (endpoint != null && getServerConfig().isStatisticsEnabled()) {
            if (endpoint.getEndpointMetrics().getFaultCount() > this.getFaultCountThreshold()
                    || endpoint.getEndpointMetrics().getRequestCount() > this.getRequestCountThreshold()
                    || endpoint.getEndpointMetrics().getMaxProcessingTime() > this.getMaxProcessingTimeThreshold()
                    || endpoint.getEndpointMetrics().getMinProcessingTime() > this.getMinProcessingTimeThreshold()
                    || endpoint.getEndpointMetrics().getAverageProcessingTime() > this.getAverageProcessingTimeThreshold()
                    || endpoint.getEndpointMetrics().getTotalProcessingTime() > this.getTotalProcessingTimeThreshold()) {
                if (!getMessage().isEmpty()) {
                    this.setResponseCode(this.getResponseStausCode(), this.getMessage());
                } else {
                    this.setResponseCode(this.getResponseStausCode());
                }
                this.setDelay(this.getDelayTime());
                return this;
            }
        }
        return null;
    }

    public int getResponseStausCode() {
        return responseStausCode;
    }

    public void setResponseStausCode(int responseStausCode) {
        this.responseStausCode = responseStausCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public long getFaultCountThreshold() {
        return faultCountThreshold;
    }

    public void setFaultCountThreshold(long faultCountThreshold) {
        this.faultCountThreshold = faultCountThreshold;
    }

    public long getRequestCountThreshold() {
        return requestCountThreshold;
    }

    public void setRequestCountThreshold(long requestCountThreshold) {
        this.requestCountThreshold = requestCountThreshold;
    }

    public long getMaxProcessingTimeThreshold() {
        return maxProcessingTimeThreshold;
    }

    public void setMaxProcessingTimeThreshold(long maxProcessingTimeThreshold) {
        this.maxProcessingTimeThreshold = maxProcessingTimeThreshold;
    }

    public long getMinProcessingTimeThreshold() {
        return minProcessingTimeThreshold;
    }

    public void setMinProcessingTimeThreshold(long minProcessingTimeThreshold) {
        this.minProcessingTimeThreshold = minProcessingTimeThreshold;
    }

    public long getTotalProcessingTimeThreshold() {
        return totalProcessingTimeThreshold;
    }

    public void setTotalProcessingTimeThreshold(long totalProcessingTimeThreshold) {
        this.totalProcessingTimeThreshold = totalProcessingTimeThreshold;
    }


    public long getAverageProcessingTimeThreshold() {
        return averageProcessingTimeThreshold;
    }

    public void setAverageProcessingTimeThreshold(long averageProcessingTimeThreshold) {
        this.averageProcessingTimeThreshold = averageProcessingTimeThreshold;
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