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

import java.net.URL;

import jakarta.activation.DataHandler;
import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.soap.SOAPBinding;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test service endpoint capability to process inlined and optimized
 * requests transparently.
 * <ul>
 * <li>Client and service endpoint have MTOM enabled (roundtrip)
 * <li>Client send inlined requests (MTOM disabled), service answers with an optimized response.
 * </ul>
 *
 * @see XOPBase
 * @author Heiko Braun <heiko.braun@jboss.com>
 * @since 05.12.2006
 */
@RunWith(Arquillian.class)
public class XOPBareTestCase extends XOPBase
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      return DeploymentArchive.createDeployment("bare");
   }

   @Before
   public void setup() throws Exception
   {
      QName serviceName = new QName("http://doclit.xop.samples.jaxws.ws.test.jboss.org/", "MTOMService");
      URL wsdlURL = new URL(baseURL + "bare?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      port = service.getPort(MTOMEndpoint.class);
      binding = (SOAPBinding)((BindingProvider)port).getBinding();
   }

   /**
    * Consumption of XOP packages (not inlined) should resolve the correct java type.
    */
   @Test
   @RunAsClient
   public void testDataHandlerRoundtrip() throws Exception
   {
      getBinding().setMTOMEnabled(true);

      DataHandler dh = new DataHandler("DataHandlerRoundtrip", "text/plain");
      DHResponse response = getPort().echoDataHandler(new DHRequest(dh));
      assertNotNull(response);
      
      Object content = getContent(response.getDataHandler());
      String contentType = response.getDataHandler().getContentType();
      
      assertEquals("Server data", content);
      assertEquals("text/plain", contentType);
   }

   /**
    * Consumption of XOP packages (not inlined) should resolve the correct java type.
    */
   @Test
   @RunAsClient
   public void testDataHandlerResponseOptimzed() throws Exception
   {
      getBinding().setMTOMEnabled(false);

      DataHandler dh = new DataHandler("DataHandlerResponseOptimzed", "text/plain");
      DHResponse response = getPort().echoDataHandler(new DHRequest(dh));
      assertNotNull(response);
      
      Object content = getContent(response.getDataHandler());
      String contentType = response.getDataHandler().getContentType();
      
      assertEquals("Server data", content);      
      assertEquals("text/plain", contentType);
   }
}
