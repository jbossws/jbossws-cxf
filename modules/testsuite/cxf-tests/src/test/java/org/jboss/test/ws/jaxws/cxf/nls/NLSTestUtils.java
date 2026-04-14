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

import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;

import javax.xml.namespace.QName;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class NLSTestUtils {


    /**
     * Attempts to access the service WSDL, which is expected to succeed, and then reads the WSDL contents to
     * validate the service name, which is expected to be decoded.
     * @param wsdlURL The WSDL URL
     * @param wsdlCharset The charset to be used when reading the WSDL contents, otherwise the JVM's default charset
     *                    would be used, which varies by platform and locale.
     *                    On certain Windows/JDK combinations, the default might be Windows-1252 or another code page
     *                    instead of UTF-8.
     * @throws IOException If the WSDL contents cannot be read.
     */
    public static void verifyWsdlServiceName(final URL wsdlURL, final Charset wsdlCharset) throws IOException {
        final HttpURLConnection conn = (HttpURLConnection) wsdlURL.openConnection();
        try {
            conn.setRequestMethod("GET");

            Assertions.assertEquals(HttpStatus.SC_OK, conn.getResponseCode(),
                    "WSDL should be accessible with URL-encoded service name");

            final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), wsdlCharset));
            final StringBuilder wsdlContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                wsdlContent.append(line);
            }
            reader.close();

            final String actualWsdlContent = wsdlContent.toString();
            Assertions.assertTrue(actualWsdlContent.contains("Caffè"),
                    "WSDL should contain the service name, see: " + actualWsdlContent);
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Verifies that the Web Service containing NLS characters can be consumed.
     * @param wsdlURL The Web Service WSDL URL
     * @param endpointURL The Web Service endpoint URL
     */
    public static void verifyNLSService(final String targetNS, final URL wsdlURL, final String endpointURL) {
        final QName serviceName = new QName(targetNS, "Caffè");
        final Service service = Service.create(wsdlURL, serviceName);
        final QName portQName = new QName(targetNS, "CaffèEndpointPort");
        final NLSEndpoint port = service.getPort(portQName, NLSEndpoint.class);

        // Set the endpoint address to the URL-encoded version to avoid client-side URL validation issues
        final BindingProvider bp = (BindingProvider) port;
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointURL);

        final String result = port.echo("Hello NLS");
        Assertions.assertEquals("NLS Echo: Hello NLS", result, "Service invocation should succeed");
    }
}
