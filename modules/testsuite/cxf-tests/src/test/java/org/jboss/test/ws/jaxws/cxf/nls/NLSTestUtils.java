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

public class NLSTestUtils {


    /**
     * Attempts to access the service WSDL, which is expected to succeed, and then reads the WSDL contents to
     * validate the service name, which is expected to be decoded.
     * @param wsdlURL The WSDL URL
     * @throws IOException If the WSDL contents cannot be read.
     */
    public static void verifyWsdlServiceName(URL wsdlURL) throws IOException {
        final HttpURLConnection conn = (HttpURLConnection) wsdlURL.openConnection();
        try {
            conn.setRequestMethod("GET");

            Assertions.assertEquals(HttpStatus.SC_OK, conn.getResponseCode(),
                    "WSDL should be accessible with URL-encoded service name");

            final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            final StringBuilder wsdlContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                wsdlContent.append(line);
            }
            reader.close();

            Assertions.assertTrue(wsdlContent.toString().contains("Caffè"), "WSDL should contain the service name");
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
