/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2023, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.wsse.policy.trust;

import io.dekorate.testing.annotation.Inject;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.LocalPortForward;
import java.net.URL;
import java.util.List;
import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import org.jboss.test.ws.jaxws.samples.wsse.policy.trust.service.ServiceIface;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.wildfly.test.cloud.common.KubernetesResource;
import org.wildfly.test.cloud.common.WildFlyCloudTestCase;
import org.wildfly.test.cloud.common.WildFlyKubernetesIntegrationTest;

import static org.wildfly.test.cloud.common.WildflyTags.KUBERNETES;

@Tag(KUBERNETES)
@WildFlyKubernetesIntegrationTest(
        buildEnabled = false,
        deployEnabled = false,
        kubernetesResources = {
                @KubernetesResource(
                        definitionLocation = "src/test/resources/kubernetes.yml"
                ),}
)
public class WSTrustK8sTestCase extends WildFlyCloudTestCase {

   //This container name has to be the same as the maven project id
   private static final String SERVICE_NAME = "jbossws-cxf-k8s-wstrust-service";
   private static final String STS_NAME = "jbossws-cxf-k8s-wstrust-sts";
   @Inject
   private KubernetesClient k8sClient;

   @Test
   public void checkServiceWithSTS() throws Exception {
      List<Pod> lst = k8sClient.pods().withLabel("app.kubernetes.io/name", SERVICE_NAME).list().getItems();
      Assertions.assertEquals(1, lst.size(), "More than one pod found with expected label " + lst);
      Pod first = lst.get(0);
      Assertions.assertNotNull(first, "pod isn't created");
      Assertions.assertEquals("Running", first.getStatus().getPhase(), "Pod isn't running");
      LocalPortForward p = k8sClient.services().withName(SERVICE_NAME).portForward(8080);
      Assertions.assertTrue(p.isAlive());
      URL serviceBaseURL = new URL("http://localhost:" + p.getLocalPort() + "/" + SERVICE_NAME);

      final QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy", "SecurityService");
      final URL wsdlURL = new URL(serviceBaseURL + "/SecurityService?wsdl");
      //TODO: look at how to resolve this to check the services are all ready
      Thread.sleep(5000);
      Service service = Service.create(wsdlURL, serviceName);
      ServiceIface proxy = (ServiceIface) service.getPort(ServiceIface.class);

      List<Pod> stslst = k8sClient.pods().withLabel("app.kubernetes.io/name", STS_NAME).list().getItems();
      Assertions.assertEquals(1, stslst.size(), "More than one STS pod found with expected label " + stslst);
      Pod firstSts = stslst.get(0);
      Assertions.assertNotNull(firstSts, "STS pod isn't created");
      Assertions.assertEquals("Running", firstSts.getStatus().getPhase(), "STS Pod isn't running");
      LocalPortForward stsPort = k8sClient.services().withName(STS_NAME).portForward(8080);
      Assertions.assertTrue(stsPort.isAlive());

      URL stsBaseURL = new URL("http://localhost:" + stsPort.getLocalPort() + "/" + STS_NAME);

      final QName stsServiceName = new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "SecurityTokenService");
      final QName stsPortName = new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "UT_Port");
      URL stsURL = new URL(stsBaseURL + "/SecurityTokenService?wsdl");
      WSTrustK8sTestUtils.setupWsseAndSTSClient(proxy, stsURL.toString(), stsServiceName, stsPortName);
      Assertions.assertEquals("WS-Trust Hello World!", proxy.sayHello());
   }
}
