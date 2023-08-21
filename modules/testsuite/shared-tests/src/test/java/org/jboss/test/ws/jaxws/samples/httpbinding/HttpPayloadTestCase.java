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
package org.jboss.test.ws.jaxws.samples.httpbinding;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import jakarta.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import jakarta.xml.ws.Dispatch;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.Service.Mode;
import jakarta.xml.ws.http.HTTPBinding;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.DOMUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Element;

/**
 * Test a Provider<SOAPMessage>
 *
 * @author Thomas.Diesler@jboss.org
 * @since 29-Jun-2006
 */
@RunWith(Arquillian.class)
public class HttpPayloadTestCase extends JBossWSTest
{
   private String reqString = "<ns1:somePayload xmlns:ns1='http://org.jboss.ws/httpbinding'>Hello</ns1:somePayload>";

   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-httpbinding-payload.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.samples.httpbinding.LogicalSourceHandler.class)
               .addClass(org.jboss.test.ws.jaxws.samples.httpbinding.ProviderBeanPayload.class)
               .addAsResource("org/jboss/test/ws/jaxws/samples/httpbinding/httpbinding-handlers.xml")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/httpbinding/shared/wsdl/HttpBinding.wsdl"), "wsdl/HttpBinding.wsdl")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/httpbinding/payload/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testWSDLAccess() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "?wsdl");
      Element wsdl = DOMUtils.parse(wsdlURL.openStream());
      assertNotNull(wsdl);
   }

   @Test
   @RunAsClient
   public void testProviderDispatch() throws Exception
   {
      Dispatch<Source> dispatch = createDispatch("ProviderEndpoint");
      Source resPayload = dispatch.invoke(new DOMSource(DOMUtils.parse(reqString)));

      Element docElement = DOMUtils.sourceToElement(resPayload);
      assertEquals("wrong namespace: " + docElement.getNamespaceURI(), "http://org.jboss.ws/httpbinding", docElement.getNamespaceURI());
      assertEquals("wrong localPart: " + docElement.getLocalName(), "somePayload", docElement.getLocalName());
      String responseString = DOMUtils.getTextContent(docElement);
      assertEquals("wrong content: " + responseString, "Hello:InboundLogicalHandler:OutboundLogicalHandler", responseString);
   }

   private Dispatch<Source> createDispatch(String target) throws MalformedURLException, JAXBException
   {
      String targetNS = "http://org.jboss.ws/httpbinding";
      QName serviceName = new QName(targetNS, "ProviderService");
      QName portName = new QName(targetNS, "ProviderPort");
      URL endpointAddress = new URL(baseURL + "/" + target);

      Service service = Service.create(serviceName);
      service.addPort(portName, HTTPBinding.HTTP_BINDING, endpointAddress.toExternalForm());

      Dispatch<Source> dispatch = service.createDispatch(portName, Source.class, Mode.PAYLOAD);
      return dispatch;
   }
}
