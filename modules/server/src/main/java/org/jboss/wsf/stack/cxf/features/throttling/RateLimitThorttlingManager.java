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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.throttling.ThrottleResponse;
import org.apache.cxf.throttling.ThrottlingManager;

public class RateLimitThorttlingManager extends ThrottleResponse implements ThrottlingManager {
    private AtomicLong[] requestTime = new AtomicLong[60];

    private AtomicInteger[] requestCount = new AtomicInteger[60];

    private AtomicBoolean firstMessage = new AtomicBoolean(false);

    private int permitsPerMin = Integer.MAX_VALUE;
    private int period = 60;
    private int permitsPerPeriod = Integer.MAX_VALUE;


    public void setPeriod(int seconds) {
        this.period = seconds;
    }

    public void setPermitsPerPeriod(int permitsPerPeriod) {
        this.permitsPerPeriod = permitsPerPeriod;
    }


    public int getPermitsPerMin() {
        return permitsPerMin;
    }

    public int getPeriod() {
        return period;
    }

    public int getPermitsPerPeriod() {
        return permitsPerPeriod;
    }

    public void setPermitsPerMin(int permitsPerMin) {
        this.period = 60;
        this.permitsPerPeriod = permitsPerMin;
    }
    public RateLimitThorttlingManager() {
        super();
        for (int i = 0; i < this.getPeriod(); i++) {
            requestTime[i] = new AtomicLong(0);
            requestCount[i] = new AtomicInteger(0);
        }
    }

    @Override
    public List<String> getDecisionPhases() {
        return Collections.singletonList(Phase.PRE_STREAM);
    }

    @Override
    public ThrottleResponse getThrottleResponse(String phase, Message m) {
        long currentTime = System.currentTimeMillis();
        int currentIndex = (int) ((currentTime / 1000) % this.getPeriod());
        requestTime[currentIndex].set(currentTime);
        requestCount[currentIndex].incrementAndGet();
        if (firstMessage.compareAndSet(false, true)) {
            return null;
        } else {
            //reset the count for the previous minutes
            for (int i = 0 ; i < this.getPeriod() ; i++) {
                AtomicLong item = requestTime[i];
                if (item.get() > 0 && (currentTime -item.get()) > this.getPeriod() * 1000) {
                    requestTime[i].set(0);
                    requestCount[i].set(0);
                }
            }
            int sum = Stream.of(requestCount).mapToInt(AtomicInteger::get).sum();
            if (sum > this.permitsPerPeriod) {
                this.setResponseCode(429);
                return this;
            }
        }
        return null;
    }
}
