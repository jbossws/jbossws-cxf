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
 * TestRule class to ignore test for some specific JDK.
 * @author <a href="mailto:jbliznak@redhat.com">Jan Bliznak</a>
 *
 */
public class IgnoreJdk implements TestRule {

    public static final IgnoreJdk IBM8 = new IgnoreJdk("IBM Corporation", "1.8");

    private String vendor;
    private String version;
    
    public IgnoreJdk(String vendor) {
        this(vendor, null);
    }

    public IgnoreJdk(String vendor, String version) {
        this.vendor = vendor;
        this.version = version;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            boolean ignored = false;

            @Override
            public void evaluate() throws Throwable {

                String jdkVersion = System.getProperty("java.version", "0");
                String jdkVendor = System.getProperty("java.vendor", "UNKNOWN");

                if (vendor != null && jdkVendor.contains(vendor)) {
                    if (version == null || jdkVersion.startsWith(version)) {
                        ignored = true;
                    }
                }
                Assume.assumeFalse(description.getClassName()
                      + " is excluded for JDK (" + vendor + (version == null ? "" : ", " + version) + ")", ignored);

                base.evaluate(); // always call base statement to continue in execution when assume passes
            }
        };
    }

}
