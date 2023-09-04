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
package org.jboss.ws.cloud.test;

import io.dekorate.testing.kubernetes.KubernetesExtension;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.jupiter.api.extension.ExtensionContext;

public class JBossWSKubernetesExtension extends KubernetesExtension {
    public JBossWSKubernetesIntegrationTestConfig getIntegrationTestConfig(ExtensionContext context) {
        // Override the super class method so we can use our own configuration
        return context.getElement()
                .map(e -> JBossWSKubernetesIntegrationTestConfig.create(e.getAnnotation(JBossWSKubernetesIntegrationTest.class)))
                .orElseThrow(
                        () -> new IllegalStateException("Not found Annotation @" + JBossWSKubernetesIntegrationTest.class.getSimpleName()));
    }
    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        JBossWSKubernetesIntegrationTestConfig config = getIntegrationTestConfig(context);
        createK8sResources(context, config);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        JBossWSKubernetesIntegrationTestConfig config = getIntegrationTestConfig(context);
        deleteK8sResources(context,config);
    }


    private void createK8sResources(ExtensionContext context, JBossWSKubernetesIntegrationTestConfig config) {
        if (config.getKuberentesResource().isEmpty()) {
            return;
        }
        KubernetesClient client = getKubernetesClient(context);
        String resource = config.getKuberentesResource();
        try {
            client.load(this.getInputStream(resource)).createOrReplace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteK8sResources(ExtensionContext context, JBossWSKubernetesIntegrationTestConfig config) {
        if (config.getKuberentesResource().isEmpty()) {
            return;
        }
        KubernetesClient client = getKubernetesClient(context);
        String resource = config.getKuberentesResource();
        try {
            client.load(this.getInputStream(resource)).delete();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private InputStream getInputStream(String resource) throws IOException {
        try {
            URL url = new URL(resource);
            return new BufferedInputStream(url.openStream());
        } catch (MalformedURLException e) {
            try {
                return new BufferedInputStream(new FileInputStream(resource));
            } catch (FileNotFoundException foe) {
                 InputStream ins = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
                 return new BufferedInputStream(ins);
            }
        }
    }
}
