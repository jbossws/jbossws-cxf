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

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpStatus;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Test case for web services with National Language Symbols (NLS) in service names.
 * Verifies that URL-encoded paths are correctly handled by {@link org.jboss.wsf.stack.cxf.transport.JBossWSDestinationRegistryImpl}.
 * 
 * @author fburzigo@ibm.com
 * @since 2026-04-14
 */
@ExtendWith(ArquillianExtension.class)
public class NLSTestCase extends JBossWSTest
{
   private static final String DEPLOYMENT_NAME = "jaxws-cxf-nls";
   private static final String CONFIGURED_DEPLOYMENT_NAME = DEPLOYMENT_NAME + "-decode-enabled";
   private static final String UNCONFIGURED_DEPLOYMENT_NAME = DEPLOYMENT_NAME + "-decode-disabled";
   private static final String TARGET_NS = "http://org.jboss.ws.jaxws.cxf/nls";
   private static final Charset ENCODED_URL_CHARSET = StandardCharsets.UTF_8;
   // The service name is "Caffè" which should be URL-encoded as "Caff%C3%A8"
   private final String encodedServiceName = URLEncoder.encode("Caffè", ENCODED_URL_CHARSET);

   /**
    * Creates an unconfigured deployment, i.e. a deployment to test web services URL NLS decoding, where the
    * {@link org.jboss.wsf.stack.cxf.client.Constants#JBWS_CXF_DECODE_URL_PATH} property is not configured
    * via the {@code jboss-webservices.xml} descriptor.
    *
    * @return A {@link WebArchive} instance which is not configured to decode URL-encoded paths containing NLS
    * characters.
    */
   @Deployment(testable = false, name = UNCONFIGURED_DEPLOYMENT_NAME)
   public static WebArchive createUnconfiguredDeployment()
   {
      return createDeployment(UNCONFIGURED_DEPLOYMENT_NAME + ".war");
   }

   /**
    * Creates an unconfigured deployment, i.e. a deployment to test web services URL NLS decoding, where the
    * {@link org.jboss.wsf.stack.cxf.client.Constants#JBWS_CXF_DECODE_URL_PATH} property is set to {@code true}
    * via the {@code jboss-webservices.xml} descriptor.
    *
    * @return A {@link WebArchive} instance which is configured to decode URL-encoded paths containing NLS
    * characters.
    */
   @Deployment(testable = false, name = CONFIGURED_DEPLOYMENT_NAME)
   public static WebArchive createConfiguredDeployment()
   {
      return createDeployment(CONFIGURED_DEPLOYMENT_NAME)
              .addAsWebInfResource(new File(
              JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/nls/WEB-INF/jboss-webservices.xml"),
              "jboss-webservices.xml");
   }

   private static WebArchive createDeployment(final String name) {
      return ShrinkWrap.create(WebArchive.class, name + ".war")
              .addClasses(NLSEndpoint.class, NLSEndpointImpl.class);
   }

   @ArquillianResource
   @OperateOnDeployment(CONFIGURED_DEPLOYMENT_NAME)
   private URL configuredDeploymentBaseURL;

   @ArquillianResource
   @OperateOnDeployment(UNCONFIGURED_DEPLOYMENT_NAME)
   private URL unconfiguredDeploymentBaseURL;

   /**
    * Verify that the WSDL for the service which name contains NLS chars can not be accessed by default.
    * @throws IOException If the WSDL URL generation fails, or if opening a connection to the WSDL fails.
    */
   @Test
   @RunAsClient
   public void testUnconfiguredDeploymentWsdlNotAvailable() throws IOException{
      // Build the URL with encoded service name
      final String endpointURL = unconfiguredDeploymentBaseURL + encodedServiceName;
      // Verify WSDL is not accessible (HTTP 500) with encoded URL
      final URL wsdlURL = new URL(endpointURL + "?wsdl");

      final HttpURLConnection conn = (HttpURLConnection) wsdlURL.openConnection();
      try {
         conn.setRequestMethod("GET");
         assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, conn.getResponseCode(),
                 "WSDL should NOT be accessible with URL-encoded service name");
      } finally {
         conn.disconnect();
      }
   }

   /**
    * Verify that the WSDL for the service which name contains NLS chars is created and can be accessed when the
    * deployment descriptor sets the {@link org.jboss.wsf.stack.cxf.client.Constants#JBWS_CXF_DECODE_URL_PATH}
    * property to {@code true}.
    * @throws IOException If the WSDL URL generation fails, or if opening a connection to the WSDL fails.
    */
   @Test
   @RunAsClient
   public void testConfiguredDeploymentWsdlAvailable() throws IOException {
      // Build the URL with encoded service name
      final String endpointURL = configuredDeploymentBaseURL + encodedServiceName;
      // Verify WSDL is accessible with encoded URL
      final URL wsdlURL = new URL(endpointURL + "?wsdl");

      NLSTestUtils.verifyWsdlServiceName(wsdlURL, ENCODED_URL_CHARSET);
   }

   /**
    * Verifies that a Web Service which {@code name} and {@code serviceName} contain NLS chars
    * can be accessed via an encoded URL when {@code org.jboss.ws.cxf.decodeUrlPath} is set to {@code true} by the
    * deployment descriptor.
    *
    * @throws MalformedURLException If the WSDL URL generation fails
    */
   @Test
   @RunAsClient
   public void testNLSServiceWithEncodedURL() throws MalformedURLException {
      // Build the URL with encoded service name
      final String endpointURL = configuredDeploymentBaseURL + encodedServiceName;
      final URL wsdlURL = new URL(endpointURL + "?wsdl");

      NLSTestUtils.verifyNLSService(TARGET_NS, wsdlURL, endpointURL);
   }
}
