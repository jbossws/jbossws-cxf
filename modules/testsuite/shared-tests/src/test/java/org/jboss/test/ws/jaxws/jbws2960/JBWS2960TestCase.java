/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.test.ws.jaxws.jbws2960;

import java.io.File;
import java.util.List;

import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;

import org.jboss.ws.api.util.DOMUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.w3c.dom.Element;

/**
 * Tests addressing mapping metadata.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
@RunWith(Arquillian.class)
public class JBWS2960TestCase extends JBossWSTest
{
   private static final QName WSAM_ACTION_QNAME = new QName("http://www.w3.org/2007/05/addressing/metadata", "Action");
   private static final QName WSAM_ADDRESSING_QNAME = new QName("http://www.w3.org/2007/05/addressing/metadata", "Addressing");
   private static final QName WSAM_NON_ANONYMOUS_RESPONSES_QNAME = new QName("http://www.w3.org/2007/05/addressing/metadata", "NonAnonymousResponses");
   private static final QName POLICY_QNAME = new QName("http://www.w3.org/ns/ws-policy", "Policy");
   private static final QName POLICY_REFERENCE_QNAME = new QName("http://www.w3.org/ns/ws-policy", "PolicyReference");
   private final File wsdlFile = JBossWSTestHelper.getResourceFile("jaxws/jbws2960/AddNumbersService.wsdl");

   @Override
   public void setUp()
   {
      assertNotNull("WSDL not found", wsdlFile);
      assertTrue("WSDL doesn't exist", wsdlFile.exists());
   }

   /*
   <definitions
     targetNamespace="http://foobar.org/"
     name="AddNumbersService"
     xmlns="http://schemas.xmlsoap.org/wsdl/"
     xmlns:wsp="http://www.w3.org/ns/ws-policy"
     xmlns:tns="http://foobar.org/"
     xmlns:wsam="http://www.w3.org/2007/05/addressing/metadata"
     xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd">
     <wsp:Policy wsu:Id="GENERATED_ID">
       <wsam:Addressing wsp:Optional="true">
         <wsp:Policy>
           <wsam:NonAnonymousResponses/>
         </wsp:Policy>
       </wsam:Addressing>
     </wsp:Policy>
     ...
     <binding name="AddNumbersPortBinding" type="tns:AddNumbers">
       <wsp:PolicyReference URI="#GENERATED_ID"/>
       ...
     </binding>
   </definitions>
    */
   @Test
   @RunAsClient
   public void testPolicyReference() throws Exception
   {
      setUp();
      Definition wsdl = getWSDLDefinition(wsdlFile.getAbsolutePath());
      List<?> definitionExtElements = wsdl.getExtensibilityElements();
      QName serviceQName = new QName("http://foobar.org/", "AddNumbersService");
      Port wsdlPort = wsdl.getService(serviceQName).getPort("AddNumbersPort");
      List<?> bindingExtElements = wsdlPort.getBinding().getExtensibilityElements();
      Element policyElement = this.getRequiredElement(definitionExtElements, POLICY_QNAME);
      Element policyReferenceElement = this.getRequiredElement(bindingExtElements, POLICY_REFERENCE_QNAME);
      String wsuIdAttrValue = policyElement.getAttributeNS("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Id");
      String uriAttrValue = policyReferenceElement.getAttribute("URI").substring(1);
      assertEquals("WS Policy mapping error", wsuIdAttrValue, uriAttrValue);

      Element addressingElement = DOMUtils.getFirstChildElement(policyElement, WSAM_ADDRESSING_QNAME);
      assertNotNull("Addressing element not found", addressingElement);
      String optionalAttributeValue = addressingElement.getAttributeNS("http://www.w3.org/ns/ws-policy", "Optional");
      assertEquals("Addressing should be optional", "true", optionalAttributeValue);
      Element nestedPolicyElement = DOMUtils.getFirstChildElement(addressingElement, POLICY_QNAME);
      assertNotNull("Nested Policy element not found", nestedPolicyElement);
      Element nonAnonymousResponsesElement = DOMUtils.getFirstChildElement(nestedPolicyElement, WSAM_NON_ANONYMOUS_RESPONSES_QNAME);
      assertNotNull("NonAnonymousResponses element not found", nonAnonymousResponsesElement);
   }

   @Test
   @RunAsClient
   public void testOperations() throws Exception
   {
      setUp();
      Definition wsdl = getWSDLDefinition(wsdlFile.getAbsolutePath());
      PortType port = wsdl.getPortType(new QName("http://foobar.org/", "AddNumbers"));
      Operation operation = null;

      // addNumbersNoAction
      operation = this.getOperation(port, "addNumbersNoAction");
      this.assertInput(operation, "http://foobar.org/AddNumbers/addNumbersNoActionRequest");
      this.assertOutput(operation, "http://foobar.org/AddNumbers/addNumbersNoActionResponse");
      this.assertFault(operation, "http://foobar.org/AddNumbers/addNumbersNoAction/Fault/AddNumbersException", "AddNumbersException");

      // addNumbersEmptyAction
      operation = this.getOperation(port, "addNumbersEmptyAction");
      this.assertInput(operation, "http://foobar.org/AddNumbers/addNumbersEmptyActionRequest");
      this.assertOutput(operation, "http://foobar.org/AddNumbers/addNumbersEmptyActionResponse");
      this.assertFault(operation, "http://foobar.org/AddNumbers/addNumbersEmptyAction/Fault/AddNumbersException", "AddNumbersException");

      // addNumbers
      operation = this.getOperation(port, "addNumbers");
      this.assertInput(operation, "http://example.com/input");
      this.assertOutput(operation, "http://example.com/output");
      this.assertFault(operation, "http://foobar.org/AddNumbers/addNumbers/Fault/AddNumbersException", "AddNumbersException");

      // addNumbers2
      operation = this.getOperation(port, "addNumbers2");
      this.assertInput(operation, "http://example.com/input2");
      this.assertOutput(operation, "http://example.com/output2");
      this.assertFault(operation, "http://foobar.org/AddNumbers/addNumbers2/Fault/AddNumbersException", "AddNumbersException");

      // addNumbers3
      operation = this.getOperation(port, "addNumbers3");
      this.assertInput(operation, "http://example.com/input3");
      this.assertOutput(operation, "http://foobar.org/AddNumbers/addNumbers3Response");
      this.assertFault(operation, "http://foobar.org/AddNumbers/addNumbers3/Fault/AddNumbersException", "AddNumbersException");

      // addNumbers4
      operation = this.getOperation(port, "addNumbers4");
      this.assertInput(operation, "http://example.com/input4");
      this.assertOutput(operation, "http://foobar.org/AddNumbers/addNumbers4Response");
      this.assertFault(operation, "http://foobar.org/AddNumbers/addNumbers4/Fault/AddNumbersException", "AddNumbersException");

      // addNumbersFault1
      operation = this.getOperation(port, "addNumbersFault1");
      this.assertInput(operation, "finput1");
      this.assertOutput(operation, "foutput1");
      this.assertFault(operation, "http://fault1", "AddNumbersException");

      // addNumbersFault2
      operation = this.getOperation(port, "addNumbersFault2");
      this.assertInput(operation, "finput2");
      this.assertOutput(operation, "foutput2");
      this.assertFault(operation, "http://fault2/addnumbers", "AddNumbersException");
      this.assertFault(operation, "http://fault2/toobignumbers", "TooBigNumbersException");

      // addNumbersFault3
      operation = this.getOperation(port, "addNumbersFault3");
      this.assertInput(operation, "finput3");
      this.assertOutput(operation, "foutput3");
      this.assertFault(operation, "http://fault3/addnumbers", "AddNumbersException");
      this.assertFault(operation, "http://foobar.org/AddNumbers/addNumbersFault3/Fault/TooBigNumbersException", "TooBigNumbersException");

      // addNumbersFault4
      operation = this.getOperation(port, "addNumbersFault4");
      this.assertInput(operation, "http://foobar.org/AddNumbers/addNumbersFault4Request");
      this.assertOutput(operation, "http://foobar.org/AddNumbers/addNumbersFault4Response");
      this.assertFault(operation, "http://fault4/addnumbers", "AddNumbersException");
      this.assertFault(operation, "http://foobar.org/AddNumbers/addNumbersFault4/Fault/TooBigNumbersException", "TooBigNumbersException");

      // addNumbersFault5
      operation = this.getOperation(port, "addNumbersFault5");
      this.assertInput(operation, "http://foobar.org/AddNumbers/addNumbersFault5Request");
      this.assertOutput(operation, "http://foobar.org/AddNumbers/addNumbersFault5Response");
      this.assertFault(operation, "http://foobar.org/AddNumbers/addNumbersFault5/Fault/AddNumbersException", "AddNumbersException");
      this.assertFault(operation, "http://fault5/toobignumbers", "TooBigNumbersException");

      // addNumbersFault6
      operation = this.getOperation(port, "addNumbersFault6");
      this.assertInput(operation, "http://foobar.org/AddNumbers/addNumbersFault6Request");
      this.assertOutput(operation, "http://foobar.org/AddNumbers/addNumbersFault6Response");
      this.assertFault(operation, "http://fault6/addnumbers", "AddNumbersException");
      this.assertFault(operation, "http://fault6/toobignumbers", "TooBigNumbersException");

      // addNumbersFault7
      operation = this.getOperation(port, "addNumbersFault7");
      this.assertInput(operation, "http://foobar.org/AddNumbers/addNumbersFault7Request");
      this.assertOutput(operation, "http://foobar.org/AddNumbers/addNumbersFault7Response");
      this.assertFault(operation, "http://foobar.org/AddNumbers/addNumbersFault7/Fault/AddNumbersException", "AddNumbersException");
      this.assertFault(operation, "http://foobar.org/AddNumbers/addNumbersFault7/Fault/TooBigNumbersException", "TooBigNumbersException");
   }

   private Operation getOperation(final PortType port, final String operationName)
   {
      return port.getOperation(operationName, null, null);
   }

   private void assertInput(final Operation operation, final String expectedValue)
   {
      QName wsamValue = (QName)operation.getInput().getExtensionAttribute(WSAM_ACTION_QNAME);

      log.debug("Validating input of operation " + operation.getName());
      Assert.assertNotNull("No WSAM attr", wsamValue);
      Assert.assertEquals("Wrong WSAM attr. value", expectedValue, wsamValue.getLocalPart());
   }

   private void assertOutput(final Operation operation, final String expectedValue)
   {
      QName wsamValue = (QName)operation.getOutput().getExtensionAttribute(WSAM_ACTION_QNAME);

      log.debug("Validating output of operation " + operation.getName());
      Assert.assertNotNull("No WSAM attr", wsamValue);
      Assert.assertEquals("Wrong WSAM attr. value", expectedValue, wsamValue.getLocalPart());
   }

   private void assertFault(final Operation operation, final String expectedValue, final String faultName)
   {
      QName wsamValue = (QName)operation.getFault(faultName).getExtensionAttribute(WSAM_ACTION_QNAME);

      log.debug("Validating fault '" + faultName + "' of operation " + operation.getName());
      Assert.assertNotNull("No WSAM attr", wsamValue);
      Assert.assertEquals("Wrong WSAM attr. value", expectedValue, wsamValue.getLocalPart());
   }

   private Definition getWSDLDefinition(final String wsdlLocation) throws Exception
   {
      WSDLFactory wsdlFactory = WSDLFactory.newInstance();
      WSDLReader wsdlReader = wsdlFactory.newWSDLReader();

      return wsdlReader.readWSDL(null, wsdlLocation);
   }

   private Element getRequiredElement(final List<?> extElements, final QName elementQName)
   {
      assertNotNull("No extensibility elements found", extElements);
      assertTrue("No extensibility elements found", extElements.size() > 0);

      Element retVal = null;
      for (int i = 0; i < extElements.size(); i++)
      {
         Object extElement = extElements.get(i);
         if (extElement instanceof UnknownExtensibilityElement)
         {
            Element candidate = ((UnknownExtensibilityElement)extElements.get(i)).getElement();
            boolean namespaceMatch = candidate.getNamespaceURI().equals(elementQName.getNamespaceURI());
            boolean nameMatch = candidate.getLocalName().equals(elementQName.getLocalPart());

            if (namespaceMatch && nameMatch)
            {
               retVal = candidate;
               break;
            }
         }
      }

      assertNotNull("Required element '" + elementQName + " ' not found", retVal);
      return retVal;
   }
}
