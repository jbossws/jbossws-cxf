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
package org.jboss.test.ws.jaxws.cxf.jbws4046;

import javax.annotation.PostConstruct;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Set;

/**
 * @author bspyrkos@redhat.com
 */
public class DemoServiceHandler implements SOAPHandler<SOAPMessageContext> {

    private int _postConstructed;

    @PostConstruct
    private void postConstruct() {
        if (_postConstructed != 0) {
            throw new RuntimeException("Post construct already called");
        }
        _postConstructed = 42;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        if (_postConstructed != 42) {
            throw new RuntimeException("Post construct was not invoked!");
        }
        return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

    @Override
    public void close(javax.xml.ws.handler.MessageContext context) {
    }

    @Override
    public Set<QName> getHeaders() {
        return null;
    }

}
