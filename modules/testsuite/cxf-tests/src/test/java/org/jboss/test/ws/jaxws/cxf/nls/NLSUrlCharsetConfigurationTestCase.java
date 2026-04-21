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

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Test case for web services with National Language Symbols (NLS) in service names, specifically to validate the
 * behavior when the {@link org.jboss.wsf.stack.cxf.client.Constants#JBWS_CXF_URL_CHARSET} system property is
 * incorrectly configured.
 * 
 * @author fburzigo@ibm.com
 * @since 2026-04-14
 */
@ExtendWith(ArquillianExtension.class)
public class NLSUrlCharsetConfigurationTestCase extends JBossWSTest
{
   private static final String DEPLOYMENT_NAME = "jaxws-cxf-nls";
   private static final String UNEXPECTED_CHARSET_DEPLOYMENT_NAME = DEPLOYMENT_NAME + "-unexpected-charset";
   private static final String INVALID_CHARSET_DEPLOYMENT_NAME = DEPLOYMENT_NAME + "-invalid-charset";
   private static final String EXPECTED_CHARSET_DEPLOYMENT_NAME = DEPLOYMENT_NAME + "-expected-charset";
   private static final String TARGET_NS = "http://org.jboss.ws.jaxws.cxf/nls";
   private static final Charset ENCODED_URL_CHARSET = StandardCharsets.UTF_8;
   // The service name is "Caffè" which should be URL-encoded as "Caff%E8", since the Charset is UTF-8
   private final String encodedServiceName = URLEncoder.encode("Caffè", ENCODED_URL_CHARSET);

   /**
    * Creates a deployment to test web services URL NLS decoding, containing a
    * {@link org.jboss.wsf.stack.cxf.client.Constants#JBWS_CXF_URL_CHARSET} system property definition which is set to
    * an unexpected value.
    *
    * @return A {@link WebArchive} instance containing a
    * {@link org.jboss.wsf.stack.cxf.client.Constants#JBWS_CXF_URL_CHARSET} system property definition which is set to
    * an unexpected value.
    */
   @Deployment(testable = false, name = UNEXPECTED_CHARSET_DEPLOYMENT_NAME)
   public static WebArchive createUnexpectedCharsetDeployment()
   {
      return createDeployment(UNEXPECTED_CHARSET_DEPLOYMENT_NAME)
              .addAsWebInfResource(new File(
                      JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/nls/WEB-INF/jboss-webservices-unexpected-url-charset.xml"),
                      "jboss-webservices.xml");
   }

   /**
    * Creates a deployment to test web services URL NLS decoding, containing a
    * {@link org.jboss.wsf.stack.cxf.client.Constants#JBWS_CXF_URL_CHARSET} system property definition which is set to
    * an invalid value.
    *
    * @return A {@link WebArchive} instance containing a
    * {@link org.jboss.wsf.stack.cxf.client.Constants#JBWS_CXF_URL_CHARSET} system property definition which is set to
    * an invalid value.
    */
   @Deployment(testable = false, name = INVALID_CHARSET_DEPLOYMENT_NAME)
   public static WebArchive createInvalidCharsetDeployment()
   {
      return createDeployment(INVALID_CHARSET_DEPLOYMENT_NAME + ".war")
              .addAsWebInfResource(new File(
                              JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/nls/WEB-INF/jboss-webservices-invalid-url-charset.xml"),
                      "jboss-webservices.xml");
   }

   /**
    * Creates a deployment to test web services URL NLS decoding, containing a
    * {@link org.jboss.wsf.stack.cxf.client.Constants#JBWS_CXF_URL_CHARSET} system property definition which is set to
    * the expected value, i.e. {@link NLSUrlCharsetConfigurationTestCase#ENCODED_URL_CHARSET}.
    *
    * @return A {@link WebArchive} instance containing a
    * {@link org.jboss.wsf.stack.cxf.client.Constants#JBWS_CXF_URL_CHARSET} system property definition which is set to
    * the expected value.
    */
   @Deployment(testable = false, name = EXPECTED_CHARSET_DEPLOYMENT_NAME)
   public static WebArchive createExpectedCharsetDeployment()
   {
      return createDeployment(EXPECTED_CHARSET_DEPLOYMENT_NAME)
              .addAsWebInfResource(new File(
                              JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/nls/WEB-INF/jboss-webservices-expected-url-charset.xml"),
                      "jboss-webservices.xml");
   }

   private static WebArchive createDeployment(final String name) {
      return ShrinkWrap.create(WebArchive.class, name + ".war")
              .addClasses(NLSEndpoint.class, NLSEndpointImpl.class);
   }

   @ArquillianResource
   @OperateOnDeployment(UNEXPECTED_CHARSET_DEPLOYMENT_NAME)
   private URL unexpectedCharsetDeploymentBaseURL;

   @ArquillianResource
   @OperateOnDeployment(INVALID_CHARSET_DEPLOYMENT_NAME)
   private URL invalidCharsetDeploymentBaseURL;

   @ArquillianResource
   @OperateOnDeployment(EXPECTED_CHARSET_DEPLOYMENT_NAME)
   private URL expectedCharsetDeploymentBaseURL;

   /**
    * Verify that the WSDL for the service which name contains NLS chars can not be accessed if
    * {@code org.jboss.wsf.stack.cxf.client.Constants#JBWS_CXF_URL_CHARSET} is set to an unexpected charset.
    *
    * @throws IOException If the WSDL URL generation fails, or if opening a connection to the WSDL fails.
    */
   @Test
   @RunAsClient
   public void testUnexpectedCharsetDeploymentWsdlNotAvailable() throws IOException{
      // Build the URL with encoded service name
      final String endpointURL = unexpectedCharsetDeploymentBaseURL + encodedServiceName;
      // Verify WSDL is not accessible (HTTP 500) with encoded URL
      final URL wsdlURL = new URL(endpointURL + "?wsdl");

      final HttpURLConnection conn = (HttpURLConnection) wsdlURL.openConnection();
      try {
         conn.setRequestMethod("GET");
         assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, conn.getResponseCode(),
                 "WSDL should NOT be accessible when org.jboss.ws.cxf.urlCharset is set to an unexpected value");
      } finally {
         conn.disconnect();
      }
   }

   /**
    * Verify that the WSDL for the service which name contains NLS chars can not be accessed if
    * {@code org.jboss.wsf.stack.cxf.client.Constants#JBWS_CXF_URL_CHARSET} is set to an invalid charset.
    *
    * @throws IOException If the WSDL URL generation fails, or if opening a connection to the WSDL fails.
    */
   @Test
   @RunAsClient
   public void testInvalidCharsetDeploymentWsdlNotAvailable() throws IOException{
      // Build the URL with encoded service name
      final String endpointURL = invalidCharsetDeploymentBaseURL + encodedServiceName;
      // Verify WSDL is not accessible (HTTP 500) with encoded URL
      final URL wsdlURL = new URL(endpointURL + "?wsdl");

      final HttpURLConnection conn = (HttpURLConnection) wsdlURL.openConnection();
      try {
         conn.setRequestMethod("GET");
         assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, conn.getResponseCode(),
                 "WSDL should NOT be accessible when org.jboss.ws.cxf.urlCharset is set to an invalid value");
      } finally {
         conn.disconnect();
      }
   }

   /**
    * Verify that the WSDL for the service which name contains NLS chars is created and can be accessed when the
    * deployment descriptor sets the {@link org.jboss.wsf.stack.cxf.client.Constants#JBWS_CXF_DECODE_URL_PATH}
    * property to the correct value, i.e. {@link NLSUrlCharsetConfigurationTestCase#ENCODED_URL_CHARSET}.
    *
    * @throws IOException If the WSDL URL generation fails, or if opening a connection to the WSDL fails.
    */
   @Test
   @RunAsClient
   public void testExpectedCharsetDeploymentWsdlAvailable() throws IOException {
      // Build the URL with encoded service name
      final String endpointURL = expectedCharsetDeploymentBaseURL + encodedServiceName;
      // Verify WSDL is accessible with encoded URL
      final URL wsdlURL = new URL(endpointURL + "?wsdl");

      NLSTestUtils.verifyWsdlServiceName(wsdlURL);
   }

   /**
    * Verifies that a Web Service which {@code name} and {@code serviceName} contain NLS chars
    * can be accessed via an encoded URL when {@code org.jboss.ws.cxf.decodeUrlPath} is set to {@code true} and
    * {@code org.jboss.ws.cxf.urlCharset} is set to the expected, no-default value.
    *
    * @throws MalformedURLException If the WSDL URL generation fails
    */
   @Test
   @RunAsClient
   public void testNLSServiceWithEncodedURL() throws MalformedURLException {
      // Build the URL with encoded service name
      final String endpointURL = expectedCharsetDeploymentBaseURL + encodedServiceName;
      final URL wsdlURL = new URL(endpointURL + "?wsdl");

      NLSTestUtils.verifyNLSService(TARGET_NS, wsdlURL, endpointURL);
   }
}
