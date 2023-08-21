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

import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMResult;
import jakarta.xml.ws.wsaddressing.W3CEndpointReference;
import jakarta.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

import org.jboss.logging.Logger;
import org.jboss.ws.common.DOMUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * W3CEndpointReferenceBuilder test.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class EndpointReferenceBuilderTestCase extends JBossWSTest
{
   private static final String URL = "http://localhost:8080/hello";
   private static final String WSDL_URL = URL + "?wsdl";
   private static final String XML_INTERFACE_NAME = "<wsam:InterfaceName xmlns:wsam=\"http://www.w3.org/2007/05/addressing/metadata\" xmlns:myns=\"http://helloservice.org/wsdl\">myns:Hello</wsam:InterfaceName>";
   private static final String XML_REF_PARAM1 = "<ns1:param1 wsa:IsReferenceParameter='true' xmlns:ns1=\"http://helloservice.org/param1\" xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">Hello</ns1:param1>";
   private static final String XML_REF_PARAM2 = "<ns2:param2 wsa:IsReferenceParameter='true' xmlns:ns2=\"http://helloservice.org/param2\" xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">World</ns2:param2>";
   private static final String MY_NS = "http://helloservice.org/wsdl";
   private static final String SERVICE_NAME = "HelloService";
   private static final String PORT_NAME = "HelloPort";
   private static final String PORT_TYPE_NAME = "Hello";
   private static final String WSA_NS = "http://www.w3.org/2005/08/addressing";
   private static final String WSAM_NS = "http://www.w3.org/2007/05/addressing/metadata";
   private static final String WSAM_PREFIX = "wsam";
   private static final String MY_PREFIX = "myns";
   private static final QName PARAM1_QNAME = new QName("http://helloservice.org/param1", "param1", "ns1");
   private static final QName PARAM2_QNAME = new QName("http://helloservice.org/param2", "param2", "ns2");
   private static final QName WSAM_SERVICE_QNAME = new QName(WSAM_NS, "ServiceName");
   private static final QName WSAM_INTERFACE_QNAME = new QName(WSAM_NS, "InterfaceName");
   private static final QName METADATA_QNAME = new QName(WSA_NS, "Metadata");
   private static final QName SERVICE_QNAME = new QName(MY_NS, SERVICE_NAME, MY_PREFIX);
   private static final QName PORT_QNAME = new QName(MY_NS, PORT_NAME, MY_PREFIX);
   private static final QName PORT_TYPE_QNAME = new QName(MY_NS, PORT_TYPE_NAME, MY_PREFIX);

   @Test
   public void testParsedInterfaceName() throws Exception
   {
      W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder(); 
      builder = builder.address(URL);
      Element element = DOMUtils.parse(XML_INTERFACE_NAME);
      builder = builder.metadata(element);
      builder = builder.serviceName(SERVICE_QNAME);
      builder = builder.endpointName(PORT_QNAME);
      builder = builder.wsdlDocumentLocation(WSDL_URL);
      element = DOMUtils.parse(XML_REF_PARAM1);
      builder = builder.referenceParameter(element);
      element = DOMUtils.parse(XML_REF_PARAM2);
      builder = builder.referenceParameter(element);
      W3CEndpointReference epr = builder.build();
      DOMResult dr = new DOMResult(); 
      epr.writeTo(dr);
      Node endpointReferenceElement = dr.getNode();
      Logger.getLogger(this.getClass()).info(DOMUtils.node2String(endpointReferenceElement));
      assertMetaData(endpointReferenceElement);
      assertRefParam(endpointReferenceElement, PARAM1_QNAME, "Hello");
      assertRefParam(endpointReferenceElement, PARAM2_QNAME, "World");
   }

   @Test
   public void testConstructedInterfaceName() throws Exception
   {
      W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder(); 
      builder = builder.address(URL);
      builder = builder.serviceName(SERVICE_QNAME);
      builder = builder.endpointName(PORT_QNAME);
      builder = builder.interfaceName(PORT_TYPE_QNAME);
      builder = builder.wsdlDocumentLocation(WSDL_URL);
      Element element = DOMUtils.parse(XML_REF_PARAM1);
      builder = builder.referenceParameter(element);
      element = DOMUtils.parse(XML_REF_PARAM2);
      builder = builder.referenceParameter(element);
      W3CEndpointReference epr = builder.build();
      DOMResult dr = new DOMResult(); 
      epr.writeTo(dr);
      Node endpointReferenceElement = dr.getNode();
      Logger.getLogger(this.getClass()).info(DOMUtils.node2String(endpointReferenceElement));
      assertMetaData(endpointReferenceElement);
      assertRefParam(endpointReferenceElement, PARAM1_QNAME, "Hello");
      assertRefParam(endpointReferenceElement, PARAM2_QNAME, "World");
   }

   private static void assertRefParam(final Node root, final QName nodeName, final String refParamValue)
   {
      Element e = (Element)DOMUtils.getFirstChildElement(root, nodeName, true);
      assertNotNull("Reference parameter " + nodeName + " not found", e);
      String actual = DOMUtils.getTextContent(e);
      if ((actual == null) || (!actual.equals(refParamValue)))
      {
         fail("Reference parameter " + nodeName + " expected value is " + refParamValue);
      }
   }
   
   private static void assertMetaData(final Node root)
   {
      Element metadataElement = (Element)DOMUtils.getFirstChildElement(root, METADATA_QNAME, true);
      String wsdlLocationValue = metadataElement.getAttributeNodeNS("http://www.w3.org/ns/wsdl-instance", "wsdlLocation").getValue();
      assertEquals("wsdlLocation mismatch", wsdlLocationValue, MY_NS + " " + WSDL_URL);
      Element serviceNameElement = (Element)DOMUtils.getFirstChildElement(metadataElement, WSAM_SERVICE_QNAME);
      assertNamespaces(serviceNameElement);
      assertEquals("wrong text content in ServiceName element", "myns:HelloService", DOMUtils.getTextContent(serviceNameElement));
      String endpointNameValue = DOMUtils.getAttributeValue(serviceNameElement, "EndpointName");
      assertNotNull("cannot find endpointName attribute value", endpointNameValue);
      assertEquals("wrong endpointName attribute value", endpointNameValue, "HelloPort");
      Element interfaceNameElement = (Element)DOMUtils.getFirstChildElement(metadataElement, WSAM_INTERFACE_QNAME);
      assertNamespaces(interfaceNameElement);
      assertEquals("wrong text content in InterfaceName element", "myns:Hello", DOMUtils.getTextContent(interfaceNameElement));
   }
   
   private static void assertNamespaces(final Element e)
   {
      String myNamespace = e.lookupNamespaceURI(MY_PREFIX);
      assertNotNull("namespace is null for prefix " + MY_PREFIX + ", isn't xalan in endorsed directory?", myNamespace);
      assertEquals("namespace mismatch", myNamespace, MY_NS);
      String wsamNamespace = e.lookupNamespaceURI(WSAM_PREFIX);
      assertNotNull("namespace is null for prefix " + WSAM_PREFIX + ", isn't xalan in endorsed directory?", wsamNamespace);
      assertEquals("namespace mismatch", wsamNamespace, WSAM_NS);
   }
}

