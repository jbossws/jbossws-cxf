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

public class JBossWSKubernetesIntegrationTestConfig {

    private String resource;
    private JBossWSKubernetesIntegrationTestConfig( String resource){
        this.resource = resource;
    }

    static JBossWSKubernetesIntegrationTestConfig create(JBossWSKubernetesIntegrationTest annotation) {
        return new JBossWSKubernetesIntegrationTestConfig(
                annotation.kubernetesResource());
    }

    /**
     * Kubernetes yaml resource file location
     * @return the url of the k8s resource file location for the cloud test
     */
    public String getKuberentesResource() {
        return this.resource;
    }
}
