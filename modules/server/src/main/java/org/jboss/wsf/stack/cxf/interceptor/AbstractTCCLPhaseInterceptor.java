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
package org.jboss.wsf.stack.cxf.interceptor;

import org.apache.cxf.common.classloader.ClassLoaderUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;

@Deprecated
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
