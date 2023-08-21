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
package org.jboss.test.ws.jaxws.samples.xop.doclit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import jakarta.activation.DataHandler;
import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.handler.Handler;
import jakarta.xml.ws.soap.MTOMFeature;
import jakarta.xml.ws.soap.SOAPBinding;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-2448] This test verify the MTOMFeature correctly enable MTOM on client side.
 *
 * @author alessio.soldano@jboss.com
 * @since 14-Jan-2009
 */
@RunWith(Arquillian.class)
public class MTOMFeatureTestCase extends JBossWSTest {

    @ArquillianResource
    private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      return DeploymentArchive.createDeployment("feature");
   }

   private MTOMEndpoint getPort(boolean mtomEnabled) throws Exception {
		QName serviceName = new QName("http://doclit.xop.samples.jaxws.ws.test.jboss.org/", "MTOMService");
		URL wsdlURL = new URL(baseURL + "bare?wsdl");

		Service service = Service.create(wsdlURL, serviceName);
		return service.getPort(MTOMEndpoint.class, new MTOMFeature(mtomEnabled));
	}

	private static void addMTOMCheckHandler(MTOMEndpoint port) {
		SOAPBinding binding = (SOAPBinding) ((BindingProvider) port).getBinding();
		@SuppressWarnings("rawtypes")
        List<Handler> handlerChain = new ArrayList<Handler>();
		handlerChain.addAll(binding.getHandlerChain());
		handlerChain.add(new MTOMCheckClientHandler());
		binding.setHandlerChain(handlerChain);
	}

   @Test
   @RunAsClient
	public void testWithMTOMRequest() throws Exception {
		DataHandler dh = new DataHandler("DataHandlerRoundtrip", "text/plain");
		MTOMEndpoint port = getPort(true);
		addMTOMCheckHandler(port);
		DHResponse response = port.echoDataHandler(new DHRequest(dh));
		assertNotNull(response);

		Object content = getContent(response.getDataHandler());
		String contentType = response.getDataHandler().getContentType();

		assertEquals("Server data", content);
		assertEquals("text/plain", contentType);
	}

   @Test
   @RunAsClient
	public void testWithoutMTOMRequest() throws Exception {
		DataHandler dh = new DataHandler("DataHandlerResponseOptimzed", "text/plain");
		DHResponse response = getPort(false).echoDataHandler(new DHRequest(dh));
		assertNotNull(response);

		Object content = getContent(response.getDataHandler());
		String contentType = response.getDataHandler().getContentType();

		assertEquals("Server data", content);
		assertEquals("text/plain", contentType);
	}

   @Test
   @RunAsClient
	public void testErrorWithoutMTOMRequest() throws Exception {
		DataHandler dh = new DataHandler("DataHandlerResponseOptimzed", "text/plain");
		MTOMEndpoint port = getPort(false);
		addMTOMCheckHandler(port);
		try
		{
			port.echoDataHandler(new DHRequest(dh));
			fail("Exception expected");
		}
		catch (Exception e)
		{
			//OK
		}
	}

	protected Object getContent(DataHandler dh) throws IOException {
		Object content = dh.getContent();

		// Metro returns an ByteArrayInputStream
		if (content instanceof InputStream) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						(InputStream) content));
				return br.readLine();
			} finally {
				((InputStream) content).close();
			}
		}
		return content;
	}
}
