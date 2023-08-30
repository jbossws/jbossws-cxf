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
package org.jboss.test.ws.jaxws.k8s;
import static org.wildfly.test.cloud.common.WildflyTags.KUBERNETES;

import io.fabric8.kubernetes.client.LocalPortForward;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import java.net.URL;
import java.util.Map;
import javax.xml.namespace.QName;
import org.jboss.test.ws.jaxws.container.Endpoint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.wildfly.test.cloud.common.WildFlyCloudTestCase;
import org.wildfly.test.cloud.common.WildFlyKubernetesIntegrationTest;

import io.dekorate.testing.annotation.Inject;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.client.KubernetesClient;

import io.fabric8.kubernetes.api.model.Pod;
import java.util.List;
import org.wildfly.test.cloud.common.KubernetesResource;


/**
 * @author <a href="mailto:ema@redhat.com">Jim Ma</a>
 *
 */
@Tag(KUBERNETES)
@WildFlyKubernetesIntegrationTest(
        buildEnabled=false,
        deployEnabled=false,
        kubernetesResources = {
                @KubernetesResource(
                        definitionLocation = "src/test/resources/kubernetes.yml"
                ),}
)
public class EndpointTestCase extends WildFlyCloudTestCase {

    private static final String APP_NAME = "jbossws-cxf-k8s-basic";

    @Inject
    private KubernetesClient k8sClient;

    @Test
    public void  checkWSEndpoint() throws Exception {
        List<Pod> lst = k8sClient.pods().withLabel("app.kubernetes.io/name", APP_NAME).list().getItems();
        Assertions.assertEquals(1, lst.size(), "More than one pod found with expected label " + lst);
        Pod first = lst.get(0);
        Assertions.assertNotNull(first, "pod isn't created");
        Assertions.assertEquals("Running", first.getStatus().getPhase(), "Pod isn't running");
        LocalPortForward p = k8sClient.services().withName(APP_NAME).portForward(8080);
        Assertions.assertTrue(p.isAlive());
        URL baseURL = new URL("http://localhost:" + p.getLocalPort() + "/" + APP_NAME + "/EndpointImpl");
        Endpoint endpoint = initPort(baseURL);
        String  echoed = endpoint.echo("from k8s pod");
        Assertions.assertEquals("Echo:from k8s pod", echoed);
    }

    private Endpoint initPort(URL baseUrl) throws Exception {
        QName serviceName = new QName("http://org.jboss.ws/cxf/container", "EndpointImplService");
        URL wsdlURL = new URL(baseUrl + "?wsdl");
        Service service = Service.create(wsdlURL, serviceName);
        Endpoint proxy = service.getPort(Endpoint.class);
        return proxy;
    }
}