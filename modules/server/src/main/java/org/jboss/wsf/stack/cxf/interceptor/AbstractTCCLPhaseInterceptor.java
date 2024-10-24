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
package org.jboss.wsf.stack.cxf.interceptor;

import org.apache.cxf.common.classloader.ClassLoaderUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;

public abstract class AbstractTCCLPhaseInterceptor<T extends Message> extends AbstractPhaseInterceptor<T> {
    public AbstractTCCLPhaseInterceptor(String phase) {
        super(null, phase, false);
    }

    public AbstractTCCLPhaseInterceptor(String i, String p) {
        super(i, p, false);
    }

    public AbstractTCCLPhaseInterceptor(String phase, boolean uniqueId) {
        super(null, phase, uniqueId);
    }

    public AbstractTCCLPhaseInterceptor(String i, String p, boolean uniqueId) {
        super(i,p, uniqueId);
    }

    @Override
    public void handleMessage(T message) throws Fault {
        ClassLoaderUtils.ClassLoaderHolder origLoader = null;
        try {
            origLoader = ClassLoaderUtils.setThreadContextClassloader(this.getClass().getClassLoader());
            handleMessageWithTCCL(message);
        } finally {
            origLoader.reset();
        }
    }
    public abstract void handleMessageWithTCCL(T message) throws Fault;
}
