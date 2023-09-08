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

import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.LocalPortForward;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class JBossWSKubernetesTest {

    @InjectKubeClient
    private KubernetesClient jbossWSKubernetesTestKubeClient;

    @BeforeEach
    public void checkServerReady() {
        waitWFLYReady(this.jbossWSKubernetesTestKubeClient, 30000);
    }
    public static boolean waitWFLYReady(KubernetesClient k8sClient, long timeout) {
        AtomicBoolean ready = new AtomicBoolean(false);
        k8sClient.services().resources().filter(serviceResource ->
                   serviceResource.get().getSpec().getPorts().stream().anyMatch(
                   servicePort -> servicePort.getTargetPort().getIntVal() == 9990)
        ).forEach(serviceResource -> {
            ServicePort servicePort = serviceResource.get().getSpec().getPorts().stream().filter(port -> port.getTargetPort().getIntVal() == 9990).findFirst().get();
            LocalPortForward p = serviceResource.portForward(servicePort.getPort());
            assertTrue(p.isAlive());
            URL url = null;
            try {
                url = new URL("http://localhost:" + p.getLocalPort() + "/health/ready");
            } catch (MalformedURLException e) {
                //
            }
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < timeout) {
                try {
                    //This is a workaround for restassured print out some warning message, we try to openstream before check response with restassured
                    //INFO: I/O exception (org.apache.http.NoHttpResponseException) caught when processing request to {}->http://localhost:53380:
                    //The target server failed to respond
                    url.openStream();
                    io.restassured.response.Response response = RestAssured.given().get(url);
                    if (response.getStatusCode() == 200) {
                        JsonPath jsonPath = response.jsonPath();
                        String readyStatus = jsonPath.getString("status");
                        if (readyStatus.equalsIgnoreCase("UP")) {
                            ready.set(true);
                            return;
                        }
                    }
                } catch (IOException e) {
                    //the WFLY is not ready.
                }
                try {
                    Thread.sleep(timeout / 5);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            ready.set(false);
        });
        //There is no WFLY instance running or the WFLY management port isn't exposed in service
        return ready.get();
    }
}
