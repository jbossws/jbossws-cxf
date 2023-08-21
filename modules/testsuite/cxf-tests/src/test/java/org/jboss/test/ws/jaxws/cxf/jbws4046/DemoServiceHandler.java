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
package org.jboss.test.ws.jaxws.cxf.jbws4046;

import jakarta.annotation.PostConstruct;
import javax.xml.namespace.QName;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;
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
    public void close(jakarta.xml.ws.handler.MessageContext context) {
    }

    @Override
    public Set<QName> getHeaders() {
        return null;
    }

}
