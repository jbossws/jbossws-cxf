/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
