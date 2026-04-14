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
package org.jboss.wsf.stack.cxf.transport;

import org.jboss.wsf.stack.cxf.JAXPDelegateClassLoader;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Internal (package level) helper class to execute privileged security actions.
 *
 * @author fburzigo@ibm.com
 * @since 2026-04-21
 */
class SecurityActions
{
    /**
     * Return the current value of the specified system property, as a string, by executing a privileged action
     *
     * @param name The name of the system property
     * @param defaultValue the default value if the property is not found
     * @return The value of the system property, or {@code defaultValue} if not found
     */
    static String getSystemProperty(final String name, final String defaultValue)
    {
        PrivilegedAction<String> action = new PrivilegedAction<String>()
        {
            public String run()
            {
                return System.getProperty(name, defaultValue);
            }
        };
        return AccessController.doPrivileged(action);
    }
}

