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
package org.jboss.wsf.test;

import org.junit.Assume;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
/**
 * TestRule class to ignore test for some specific system property setting. 
 * @author <a href="mailto:ema@redhat.com">Jim Ma</a>
 *
 */
public class IgnoreEnv implements TestRule {

    public static final IgnoreEnv IPV6 = new IgnoreEnv("java.net.preferIPv6Addresses", "true");
    public static final IgnoreEnv IPV4 = new IgnoreEnv("java.net.preferIPv4Stack", "true");
    private String key;
    private String value;
    public IgnoreEnv(String key) {
        this(key, null);
    }

    public IgnoreEnv(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            boolean ignored = false;

            @Override
            public void evaluate() throws Throwable {

                if (value != null) {
                    if (System.getProperty(key) != null && value.equals(System.getProperty(key))) {
                        ignored = true;
                    }
                } else {
                    if (System.getProperty(key) != null) {
                        ignored = true;
                    }
                }
                Assume.assumeFalse(description.getClassName() + " is excluded for system env (" + key + ")", ignored);

                base.evaluate(); // always call base statement to continue in execution when assume passes
            }
        };
    }

}
