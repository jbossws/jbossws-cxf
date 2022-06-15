package org.jboss.wsf.test;

import org.junit.Assume;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class EnableOnJDK implements TestRule {

    public static final EnableOnJDK ON_JDK17 = new EnableOnJDK("17");

    private String version;

    public EnableOnJDK(String version) {
        this.version = version;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            boolean enabled = false;

            @Override
            public void evaluate() throws Throwable {

                String jdkVersion = System.getProperty("java.version", "0");

                if (jdkVersion.startsWith(version)) {
                    enabled = true;
                }

                Assume.assumeTrue(description.getClassName()
                        + " is enabled for JDK (" + version + ")", enabled);
                base.evaluate(); // always call base statement to continue in execution when assume passes
            }
        };
    }

}

