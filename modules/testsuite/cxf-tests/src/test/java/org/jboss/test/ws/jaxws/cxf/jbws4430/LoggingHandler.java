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
package org.jboss.test.ws.jaxws.cxf.jbws4430;

import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Collections;
import java.util.logging.Logger;
import java.util.Set;
import javax.xml.namespace.QName;

import jakarta.enterprise.inject.spi.CDI;

public class LoggingHandler implements SOAPHandler<SOAPMessageContext> {
    
    private static final Logger logger = Logger.getLogger(LoggingHandler.class.getName());
    
    private void testDelegateBean() {
        
        CDI cdi = CDI.current();
        if(cdi == null)
            throw new RuntimeException("Unable to get CDI.current");
        
        DelegateBean delegateBean = (DelegateBean) cdi.select(DelegateBean.class).get();
        if(delegateBean == null)
            throw new RuntimeException("Unable to get DelegateBean via CDI");
        
        logger.info("delegateBean = " + delegateBean);
    }
    
    @Override
    public boolean handleMessage(SOAPMessageContext soapMessageContext) {
        boolean isOutBound = (boolean) soapMessageContext.get(SOAPMessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (isOutBound) {
            testDelegateBean();
            //new DelegateBean();
        }
        return true;
    }

    @Override
    public Set<QName> getHeaders() {
        testDelegateBean();
        //new DelegateBean();
        return Collections.emptySet();
    }

    @Override
    public boolean handleFault(SOAPMessageContext soapMessageContext) {
        testDelegateBean();
        //new DelegateBean();
        return true;
    }

    @Override
    public void close(MessageContext messageContext) {
        testDelegateBean();
        //new DelegateBean();
    }
}
