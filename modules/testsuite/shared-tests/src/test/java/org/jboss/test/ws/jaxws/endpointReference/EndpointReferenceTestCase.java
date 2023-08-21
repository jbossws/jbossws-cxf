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
package org.jboss.test.ws.jaxws.endpointReference;

import java.net.InetAddress;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Dispatch;
import jakarta.xml.ws.EndpointReference;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.Service.Mode;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.soap.AddressingFeature;
import jakarta.xml.ws.wsaddressing.W3CEndpointReference;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.ws.common.DOMUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Test EPR related methods
 *
 * @author alessio.soldano@jboss.com
 * @author ropalka@redhat.com
 * @since 13-Jan-2009
 */
@RunWith(Arquillian.class)
public class EndpointReferenceTestCase extends JBossWSTest
{
   private final String WSDL_NS = "http://org.jboss.ws/endpointReference";
   private final QName SERVICE_QNAME = new QName(WSDL_NS, "EndpointService");
   private final QName PORT_QNAME = new QName(WSDL_NS, "EndpointPort");

   private static Service service;

   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static JavaArchive createDeployments() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-endpointReference.jar");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.endpointReference.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.endpointReference.EndpointImpl.class);
      return archive;
   }

   @Before
   public void setup() throws Exception
   {
      if (service == null) {
         URL wsdlURL = new URL(baseURL + "/jaxws-endpointReference?wsdl");
         service = Service.create(wsdlURL, SERVICE_QNAME);
      }
   }
   
   @AfterClass
   public static void cleanup() {
      service = null;
   }

   @Test
   @RunAsClient
   public void testDispatch() throws Exception
   {
      final Dispatch<Source> dispatch = service.createDispatch(PORT_QNAME, Source.class, Mode.PAYLOAD);
      this.validateEndpointReferences(dispatch);
   }

   @Test
   @RunAsClient
   public void testDispatchWithFeatures() throws Exception
   {
      final Dispatch<Source> dispatch = service.createDispatch(PORT_QNAME, Source.class, Mode.PAYLOAD, new AddressingFeature(false, false));
      this.validateEndpointReferences(dispatch);
   }

   @Test
   @RunAsClient
   public void testPort() throws Exception
   {
      final Endpoint port = service.getPort(Endpoint.class);
      this.validateEndpointReferences((BindingProvider)port);
   }

   private void validateEndpointReferences(final BindingProvider bp) throws Exception
   {
      assertEndpointReference(bp.getEndpointReference());
      assertEndpointReference(bp.getEndpointReference(W3CEndpointReference.class));
      try
      {
         bp.getEndpointReference(MyEndpointReference.class);
         fail("WebServiceException expected");
      }
      catch (WebServiceException e)
      {
         //NOP: the provided EndpointReference is not supported by the implementation
      }
      catch (Throwable t)
      {
         fail("WebServiceException expected, got " + t);
      }

      Endpoint port = bp.getEndpointReference().getPort(Endpoint.class);
      assertNotNull(port);
      String retObj = port.echo("Hello");
      assertEquals("Hello", retObj);

      port = bp.getEndpointReference(W3CEndpointReference.class).getPort(Endpoint.class);
      assertNotNull(port);
      retObj = port.echo("Hello");
      assertEquals("Hello", retObj);
   }

   private void assertEndpointReference(EndpointReference epr) throws Exception
   {
      assertEquals(W3CEndpointReference.class.getName(), epr.getClass().getName());
      Element endpointReference = DOMUtils.parse(epr.toString());
      assertEquals("EndpointReference", endpointReference.getNodeName());
      assertEquals("http://www.w3.org/2005/08/addressing", endpointReference.getAttribute("xmlns"));
      NodeList addresses = endpointReference.getElementsByTagName("Address");
      assertEquals(1, addresses.getLength());
      URL eprAddress = new URL(addresses.item(0).getFirstChild().getNodeValue());
      URL ENDPOINT_ADDRESS = new URL(baseURL.toString() + "/jaxws-endpointReference");

      //compare hosts' IPs
      String eprAddressHost = InetAddress.getByName(eprAddress.getHost()).getHostAddress();
      String endpointAddressHost = InetAddress.getByName(ENDPOINT_ADDRESS.getHost()).getHostAddress();
      assertEquals(eprAddressHost, endpointAddressHost);
      assertEquals(ENDPOINT_ADDRESS.toString().replace(ENDPOINT_ADDRESS.getHost(), eprAddress.getHost()), eprAddress.toString());
   }

   private static class MyEndpointReference extends EndpointReference
   {
      @Override
      public void writeTo(Result result)
      {
         throw new UnsupportedOperationException();
      }
   }
}
