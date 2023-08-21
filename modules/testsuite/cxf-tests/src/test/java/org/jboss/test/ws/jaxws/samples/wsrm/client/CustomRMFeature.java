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
package org.jboss.test.ws.jaxws.samples.wsrm.client;

import org.apache.cxf.ws.rm.feature.RMFeature;
import org.apache.cxf.ws.rm.manager.AcksPolicyType;
import org.apache.cxf.ws.rm.manager.DestinationPolicyType;
import org.apache.cxf.ws.rmp.v200502.RMAssertion;
import org.apache.cxf.ws.rmp.v200502.RMAssertion.AcknowledgementInterval;

/**
 * A custom version of RMFeature that
 * sets a bunch of RM options
 *
 * @author alessio.soldano@jboss.com
 * @since 11-Mar-2015
 */
public class CustomRMFeature extends RMFeature
{
   public CustomRMFeature() {
      super();
      RMAssertion rma = new RMAssertion();
      RMAssertion.BaseRetransmissionInterval bri = new RMAssertion.BaseRetransmissionInterval();
      bri.setMilliseconds(4000L);
      rma.setBaseRetransmissionInterval(bri);
      AcknowledgementInterval ai = new AcknowledgementInterval();
      ai.setMilliseconds(2000L);
      rma.setAcknowledgementInterval(ai);
      super.setRMAssertion(rma);
      DestinationPolicyType dp = new DestinationPolicyType();
      AcksPolicyType ap = new AcksPolicyType();
      ap.setIntraMessageThreshold(0);
      dp.setAcksPolicy(ap);
      super.setDestinationPolicy(dp);
   }
}
