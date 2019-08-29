/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2019, Red Hat Middleware LLC, and individual contributors
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
