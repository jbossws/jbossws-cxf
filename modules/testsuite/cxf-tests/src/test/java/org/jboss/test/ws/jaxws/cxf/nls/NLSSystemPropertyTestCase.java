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
package org.jboss.test.ws.jaxws.cxf.nls;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Test case for web services with National Language Symbols (NLS) in service names using system property configuration.
 * Tests the global system property {@code org.jboss.ws.cxf.decodeUrlPath} set at WildFly startup.
 *
 * @author fburzigo@ibm.com
 * @since 2026-04-16
 */
@ExtendWith(ArquillianExtension.class)
public class NLSSystemPropertyTestCase extends JBossWSTest
{
   private static final String CONTAINER_NAME = "jboss-sysprop";
   private static final String DEPLOYMENT_NAME = "jaxws-cxf-nls-sysprop";
   private static final String TARGET_NS = "http://org.jboss.ws.jaxws.cxf/nls";
   // The service name is "Caffè" which should be URL-encoded as "Caff%C3%A8"
   private final String encodedServiceName = URLEncoder.encode("Caffè", StandardCharsets.UTF_8);

   @ArquillianResource
   private ContainerController containerController;

   @ArquillianResource
   private Deployer deployer;

   /**
    * Creates a deployment for testing with system property enabled.
    * This deployment does NOT include jboss-webservices.xml.
    *
    * @return A {@link WebArchive} instance
    */
   @Deployment(name = DEPLOYMENT_NAME, managed = false, testable = false)
   @TargetsContainer(CONTAINER_NAME)
   public static WebArchive createDeployment()
   {
      return ShrinkWrap.create(WebArchive.class, DEPLOYMENT_NAME + ".war")
              .addClasses(NLSEndpoint.class, NLSEndpointImpl.class);
   }

   @BeforeEach
   public void startContainer() {
      if (!containerController.isStarted(CONTAINER_NAME)) {
         containerController.start(CONTAINER_NAME);
      }
      deployer.deploy(DEPLOYMENT_NAME);
   }

   @AfterEach
   public void stopContainer() {
      try {
         deployer.undeploy(DEPLOYMENT_NAME);
      } catch (Exception e) {
         // ignore
      }
      if (containerController.isStarted(CONTAINER_NAME)) {
         containerController.stop(CONTAINER_NAME);
      }
   }

   /**
    * Verify that the WSDL for the service which name contains NLS chars is accessible when the
    * {@code org.jboss.ws.cxf.decodeUrlPath} system property is set to {@code true} at server startup.
    * @throws IOException If the WSDL URL generation fails, or if opening a connection to the WSDL fails.
    */
   @Test
   @RunAsClient
   public void testWsdlAvailableWithSystemProperty() throws IOException {
      // Build the URL with encoded service name
      final int port = getServerPort("cxf-tests", CONTAINER_NAME);
      final URL baseURL = new URL("http://" + getServerHost() + ":" + port + "/" + DEPLOYMENT_NAME + "/");
      final String endpointURL = baseURL + encodedServiceName;
      // Verify WSDL is accessible with encoded URL
      final URL wsdlURL = new URL(endpointURL + "?wsdl");

      NLSTestUtils.verifyWsdlServiceName(wsdlURL);
   }

   /**
    * Verifies that a Web Service which {@code name} and {@code serviceName} contain NLS chars
    * can be accessed via an encoded URL when the {@code org.jboss.ws.cxf.decodeUrlPath} system property
    * is set to {@code true} at server startup.
    *
    * @throws MalformedURLException If the WSDL URL generation fails
    */
   @Test
   @RunAsClient
   public void testNLSServiceWithEncodedURLAvailableViaSystemProperty() throws MalformedURLException {
      // Build the URL with encoded service name
      final int port = getServerPort("cxf-tests", CONTAINER_NAME);
      final URL baseURL = new URL("http://" + getServerHost() + ":" + port + "/" + DEPLOYMENT_NAME + "/");
      final String endpointURL = baseURL + encodedServiceName;
      final URL wsdlURL = new URL(endpointURL + "?wsdl");

      NLSTestUtils.verifyNLSService(TARGET_NS, wsdlURL, endpointURL);
   }
}
