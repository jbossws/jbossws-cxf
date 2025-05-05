/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2024, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.jbws4430;

import javax.enterprise.inject.spi.CDI;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Collections;
import java.util.logging.Logger;
import java.util.Set;
import javax.xml.namespace.QName;


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
