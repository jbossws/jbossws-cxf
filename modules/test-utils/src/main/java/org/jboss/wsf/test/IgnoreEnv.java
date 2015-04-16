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
            }
        };
    }

}
