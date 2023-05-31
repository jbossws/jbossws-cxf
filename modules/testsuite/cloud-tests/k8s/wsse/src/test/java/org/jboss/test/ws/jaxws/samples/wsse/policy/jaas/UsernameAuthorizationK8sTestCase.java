/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.wsse.policy.jaas;

import static org.wildfly.test.cloud.common.WildflyTags.KUBERNETES;

import io.fabric8.kubernetes.client.LocalPortForward;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import java.net.URL;
import java.util.Map;
import javax.xml.namespace.QName;
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
 */
@Tag(KUBERNETES)
@WildFlyKubernetesIntegrationTest(
        buildEnabled = false,
        deployEnabled = false,
        kubernetesResources = {
                @KubernetesResource(
                        definitionLocation = "src/test/resources/kubernetes.yml"
                ),}
)
public class UsernameAuthorizationK8sTestCase extends WildFlyCloudTestCase {

    private static final String APP_NAME = "jbossws-cxf-k8s-wsse";


    @Inject
    private KubernetesClient k8sClient;

    @Test
    public void checkWSEndpoint() throws Exception {
        List<Pod> lst = k8sClient.pods().withLabel("app.kubernetes.io/name", APP_NAME).list().getItems();
        Assertions.assertEquals(1, lst.size(), "More than one pod found with expected label " + lst);
        Pod first = lst.get(0);
        Assertions.assertNotNull(first, "pod isn't created");
        Assertions.assertEquals("Running", first.getStatus().getPhase(), "Pod isn't running");
        LocalPortForward p = k8sClient.services().withName(APP_NAME).portForward(8080);
        Assertions.assertTrue(p.isAlive());
        URL baseURL = new URL("http://localhost:" + p.getLocalPort() + "/" + APP_NAME);
        ServiceIface proxy = initPort(baseURL);
        setupWsse(proxy, "kermit");
        Assertions.assertEquals("Secure Hello World!", proxy.sayHello());
        testUnauthenticated(proxy);
        testUnauthorized(proxy);
    }



    public void testUnauthenticated(ServiceIface proxy) throws Exception {
        setupWsse(proxy, "snoopy");
        try {
            proxy.sayHello();
            Assertions.fail("User snoopy shouldn't be authenticated.");
        } catch (Exception e) {
            //OK
        }
    }

    public void testUnauthorized(ServiceIface proxy) throws Exception {
        setupWsse(proxy, "kermit");
        try {
            proxy.greetMe();
            Assertions.fail("User kermit shouldn't be authorized to call greetMe().");
        } catch (Exception e) {
            Assertions.assertEquals("Unauthorized", e.getMessage());
        }
    }

    private ServiceIface initPort(URL baseUrl) throws Exception {

        QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService");
        URL wsdlURL = new URL(baseUrl + "/jaxws-samples-wsse-policy-username-jaas?wsdl");
        Service service = Service.create(wsdlURL, serviceName);
        ServiceIface proxy = (ServiceIface) service.getPort(ServiceIface.class);
        return proxy;
    }



    private void setupWsse(ServiceIface proxy, String username) {
        ((BindingProvider) proxy).getRequestContext().put("security.username", username);
        ((BindingProvider) proxy).getRequestContext().put("security.callback-handler", "org.jboss.test.ws.jaxws.samples.wsse.policy.jaas.UsernamePasswordCallback");
    }
}
