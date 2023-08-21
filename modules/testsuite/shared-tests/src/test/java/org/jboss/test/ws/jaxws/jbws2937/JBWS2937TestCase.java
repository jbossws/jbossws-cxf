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
package org.jboss.test.ws.jaxws.jbws2937;

import java.io.StringReader;
import java.net.URL;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Dispatch;
import jakarta.xml.ws.EndpointReference;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.Service.Mode;
import jakarta.xml.ws.WebServiceFeature;
import jakarta.xml.ws.soap.AddressingFeature;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.ws.common.DOMUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * [JBWS-2937] Cannot create dispatch object using EPR based jakarta.xml.ws.Service.createDispatch methods.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
@RunWith(Arquillian.class)
public final class JBWS2937TestCase extends JBossWSTest
{
   private final WebServiceFeature[] ADDRESSING_ENABLED = { new AddressingFeature(true) };
   private final WebServiceFeature[] ADDRESSING_DISABLED = { new AddressingFeature(false) };
   private final String NAMESPACE_URI = "http://jboss.org/jbws2937";
   private final String XML = "<ns1:echo xmlns:ns1='http://jboss.org/jbws2937'>" +
      " <arg0>" +
      "  <string>Kermit</string>" +
      "  <qname>TheFrog</qname>" +
      " </arg0>" +
      "</ns1:echo>";
   private final QName SERVICE_QNAME = new QName(NAMESPACE_URI, "EndpointService");
   private final QName PORT_QNAME = new QName(NAMESPACE_URI, "EndpointPort");
   private static Service service;
   private static Endpoint proxy;
   private static EndpointReference epr;

   @ArquillianResource
   private URL baseURL;
   
   @Deployment(name="jaxws-jbws2937", testable = false)
   public static JavaArchive createDeployment2() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws2937.jar");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws2937.EndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2937.UserType.class);
      return archive;
   }
   
   @Before
   public void setup() throws Exception
   {
      if (service == null) {
         URL wsdlURL = new URL("http://" + baseURL.getHost() + ":" + baseURL.getPort() + "/jaxws-jbws2937" + "?wsdl");
         service = EndpointService.create(wsdlURL, SERVICE_QNAME);
         proxy = (Endpoint)service.getPort(PORT_QNAME, Endpoint.class);
         epr = ((BindingProvider)proxy).getEndpointReference();
      }
   }
   
   @AfterClass
   public static void cleanup() {
      epr = null;
      proxy = null;
      service = null;
   }

   @Test
   @RunAsClient
   public void testProxy() throws Exception
   {
      final UserType user = createUser();
      final UserType response = proxy.echo(user);
      assertEquals(user, response);
   }

   @Test
   @RunAsClient
   public void testCreateDispatchUsingEPRAndSource() throws Exception
   {
      Dispatch<Source> dispatch = service.createDispatch(PORT_QNAME, Source.class, Mode.PAYLOAD);
      assertNotNull("Dispatch is null", dispatch);
      this.invokeSourceDispatch(dispatch);
      epr = dispatch.getEndpointReference();
      printEPR(epr);

      dispatch = service.createDispatch(epr, Source.class, Service.Mode.PAYLOAD, ADDRESSING_ENABLED);
      assertNotNull("Dispatch is null", dispatch);
      this.invokeSourceDispatch(dispatch);
      epr = dispatch.getEndpointReference();
      printEPR(epr);

      dispatch = service.createDispatch(epr, Source.class, Service.Mode.PAYLOAD, ADDRESSING_DISABLED);
      assertNotNull("Dispatch is null", dispatch);
      this.invokeSourceDispatch(dispatch);
      epr = dispatch.getEndpointReference();
      printEPR(epr);
   }

   @Test
   @RunAsClient
   public void testCreateDispatchUsingEPRAndJAXBContext() throws Exception
   {
      Dispatch<Object> dispatch = service.createDispatch(PORT_QNAME, this.createJAXBContext(), Mode.PAYLOAD);
      assertNotNull("Dispatch is null", dispatch);
      this.invokeObjectDispatch(dispatch);
      epr = dispatch.getEndpointReference();
      printEPR(epr);

      dispatch = service.createDispatch(epr, this.createJAXBContext(), Service.Mode.PAYLOAD, ADDRESSING_ENABLED);
      assertNotNull("Dispatch is null", dispatch);
      this.invokeObjectDispatch(dispatch);
      epr = dispatch.getEndpointReference();
      printEPR(epr);

      dispatch = service.createDispatch(epr, this.createJAXBContext(), Service.Mode.PAYLOAD, ADDRESSING_DISABLED);
      assertNotNull("Dispatch is null", dispatch);
      this.invokeObjectDispatch(dispatch);
      epr = dispatch.getEndpointReference();
      printEPR(epr);
   }
   
   private UserType createUser() {
      // prepare request object
      UserType user = new UserType();
      user.setString("Kermit");
      user.setQname(new QName("TheFrog"));
      return user;
   }
   
   private JAXBContext createJAXBContext()
   {
      try
      {
         return JAXBContext.newInstance(ObjectFactory.class);
      }
      catch (JAXBException e)
      {
         throw new RuntimeException(e.getMessage(), e);
      }
   }

   private void invokeObjectDispatch(final Dispatch<Object> dispatch) throws Exception
   {
      Echo request = new Echo();
      final UserType user = createUser();
      request.setArg0(user);
      EchoResponse response = (EchoResponse)dispatch.invoke(request);
      assertEquals(response.getReturn(), user);
   }
   
   private void invokeSourceDispatch(final Dispatch<Source> dispatch) throws Exception
   {
      Source request = new StreamSource(new StringReader(XML));
      Source response = dispatch.invoke(request);
      verifyResponse(response);
   }

   private void verifyResponse(final Source result) throws Exception
   {
      final Element echoResponseElement = DOMUtils.sourceToElement(result);
      Logger.getLogger(this.getClass()).info(DOMUtils.node2String(echoResponseElement));
      assertNotNull("echoResponse element is null", echoResponseElement);
      // validate return element
      final Element returnElement = DOMUtils.getFirstChildElement(echoResponseElement);
      assertNotNull("return element is null", returnElement);
      assertEquals("return", returnElement.getNodeName());
      // validate string element
      final Element stringElement = DOMUtils.getFirstChildElement(returnElement, "string");
      assertNotNull("string element is null", stringElement);
      assertEquals("string", stringElement.getNodeName());
      assertEquals("Kermit", stringElement.getTextContent());
      // validate string element
      final Element qnameElement = DOMUtils.getFirstChildElement(returnElement, "qname");
      assertNotNull("qname element is null", qnameElement);
      assertEquals("qname", qnameElement.getNodeName());
      assertEquals("TheFrog", qnameElement.getTextContent());
   }

   private void printEPR(final EndpointReference epr) throws Exception
   {
      DOMResult dr = new DOMResult(); 
      epr.writeTo(dr);
      Node endpointReferenceElement = dr.getNode();
      Logger.getLogger(this.getClass()).info(DOMUtils.node2String(endpointReferenceElement));
   }

   private void assertEquals(final UserType user1, final UserType user2)
   {
      assertEquals("user.string differs", user1.getString(), user2.getString());
      assertEquals("user.qname differs", user1.getQname(), user2.getQname());
   }
}
