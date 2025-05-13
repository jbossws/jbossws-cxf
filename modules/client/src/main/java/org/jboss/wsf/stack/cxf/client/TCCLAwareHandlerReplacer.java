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
package org.jboss.wsf.stack.cxf.client;

import jakarta.xml.ws.handler.Handler;
import jakarta.xml.ws.handler.LogicalHandler;
import jakarta.xml.ws.handler.soap.SOAPHandler;

import java.util.function.UnaryOperator;

/**
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
@SuppressWarnings("rawtypes")
final class TCCLAwareHandlerReplacer implements UnaryOperator<Handler> {

    static final UnaryOperator<Handler> INSTANCE = new TCCLAwareHandlerReplacer();

    private TCCLAwareHandlerReplacer() {
        // forbidden instantiation
    }

    @Override
    public Handler apply(final Handler handler) {
        if (handler instanceof TCCLAwareLogicalHandler) return handler;
        if (handler instanceof LogicalHandler) return new TCCLAwareLogicalHandler((LogicalHandler)handler);
        if (handler instanceof SOAPHandler) return new TCCLAwareSOAPHandler((SOAPHandler)handler);;
        throw new IllegalArgumentException();
    }

}
