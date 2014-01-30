/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.wsse.policy.trust;

import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.cxf.message.Message;
import org.apache.cxf.interceptor.Fault;

import java.util.ArrayList;
import java.util.Set;

/**
 * User: rsearls@redhat.com
 * Date: 1/26/14
 */
public class ActAsOutInterceptor extends AbstractPhaseInterceptor<Message> {

    public ActAsOutInterceptor () {
        super(Phase.SETUP);
    }

    @Override
    public void handleMessage(Message message) throws Fault {

        String tmpStr = "<wst:ActAs xmlns:wst=\"http://docs.oasis-open.org/ws-sx/ws-trust/200512\">myactaskey</wst:ActAs>";
        message.put(SecurityConstants.STS_TOKEN_ACT_AS, tmpStr);
    }

    @Override
    public void handleFault(Message message) {
    }
}
