package org.jboss.ws.cloud.test;

public interface JBossWSServerContainer {
    default String getContainerName() {
        return System.getProperty("project.artifactId");
    }
}
