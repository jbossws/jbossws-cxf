/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxrpc.samples.message;

import java.io.ByteArrayInputStream;
import java.net.URL;

import javax.naming.InitialContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;
import org.jboss.ws.api.util.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test unstructured message processing
 *
 * @author Thomas.Diesler@jboss.org
 * @since 26-Nov-2004
 */
public class MessageTestCase extends JBossWSTest
{
   private final String TARGET_ENDPOINT = "http://" + getServerHost() + ":8080/jaxrpc-samples-message";
   private static final String TARGET_NAMESPACE = "http://org.jboss.ws/samples/message";

   /** Deploy the test ear */
   public static Test suite() throws Exception
   {
      return new JBossWSTestSetup(MessageTestCase.class, "jaxrpc-samples-message.war, jaxrpc-samples-message-client.jar");
   }

   /** Use the SAAJ API to send the SOAP message.
    * This simulates an external client and tests server side message handling.
    */
   public void testSAAJClientFromEnvelope() throws Exception
   {
      MessageFactory mf = MessageFactory.newInstance();
      SOAPMessage reqMsg = mf.createMessage();

      DocumentBuilder builder = getDocumentBuilder();
      Document doc = builder.parse(new ByteArrayInputStream(MessageTestService.request.getBytes()));
      reqMsg.getSOAPBody().addDocument(doc);

      SOAPConnectionFactory conFactory = SOAPConnectionFactory.newInstance();
      SOAPConnection con = conFactory.createConnection();
      SOAPMessage resMsg = con.call(reqMsg, new URL(TARGET_ENDPOINT));

      SOAPBody soapBody = resMsg.getSOAPBody();
      SOAPElement soapElement = (SOAPElement)soapBody.getChildElements().next();

      validateResponse(soapElement);
   }

   /** Use the SAAJ API to send the SOAP message.
    * This simulates an external client and tests server side message handling.
    */
   public void testSAAJClientFromBody() throws Exception
   {
      MessageFactory mf = MessageFactory.newInstance();
      SOAPMessage reqMsg = mf.createMessage();

      DocumentBuilder builder = getDocumentBuilder();
      Document doc = builder.parse(new ByteArrayInputStream(MessageTestService.request.getBytes()));
      reqMsg.getSOAPBody().addDocument(doc);

      SOAPConnectionFactory conFactory = SOAPConnectionFactory.newInstance();
      SOAPConnection con = conFactory.createConnection();
      SOAPMessage resMsg = con.call(reqMsg, new URL(TARGET_ENDPOINT));

      SOAPBody soapBody = resMsg.getSOAPBody();
      SOAPElement soapElement = (SOAPElement)soapBody.getChildElements().next();

      validateResponse(soapElement);
   }

   /** Use the JBoss generated dynamic proxy send the SOAP message.
    * This tests server/client side message handling.
    */
   public void testProcessElement() throws Exception
   {
      MessageTestService port = getPort();

      DocumentBuilder builder = getDocumentBuilder();
      Document doc = builder.parse(new ByteArrayInputStream(MessageTestService.request.getBytes()));
      Element reqElement = doc.getDocumentElement();

      Element resElement = port.processElement(convertToSOAPElement(reqElement));
      validateResponse(resElement);
   }

   private MessageTestService getPort() throws Exception
   {
      ServiceFactory serviceFactory = ServiceFactory.newInstance();
      Service service = serviceFactory.createService(new URL(TARGET_ENDPOINT + "?wsdl"), new QName(TARGET_NAMESPACE, "MessageService"));
      return (MessageTestService)service.getPort(new QName(TARGET_NAMESPACE, "MessageTestServicePort"), MessageTestService.class);
   }
   
   private SOAPElement convertToSOAPElement(Element reqElement) throws TransformerException, SOAPException
   {
      SOAPElement parent = SOAPFactory.newInstance().createElement("dummy");
      TransformerFactory factory = TransformerFactory.newInstance();
      Transformer transformer = factory.newTransformer();
      transformer.transform(new DOMSource(reqElement), new DOMResult(parent));
      return (SOAPElement)parent.getChildElements().next();
   }

   private DocumentBuilder getDocumentBuilder() throws ParserConfigurationException
   {
      // Setup document builder
      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
      docBuilderFactory.setNamespaceAware(true);

      DocumentBuilder builder = docBuilderFactory.newDocumentBuilder();
      return builder;
   }

   private void validateResponse(Element resEl) throws Exception
   {
      QName expName = new QName(MessageTestService.TARGET_NAMESPACE, "Response", MessageTestService.PREFIX_1);
      QName elementName = new QName(resEl.getNamespaceURI(), resEl.getLocalName(), resEl.getPrefix());
      assertEquals(expName, elementName);

      expName = new QName("POID");
      Element poidEl = DOMUtils.getFirstChildElement(resEl, expName);
      elementName = new QName(poidEl.getLocalName());
      assertEquals(expName, elementName);

      String elementValue = DOMUtils.getTextContent(poidEl);
      assertEquals("12345", elementValue);

      expName = new QName("Status");
      Element statusEl = DOMUtils.getFirstChildElement(resEl, expName);
      elementName = new QName(statusEl.getLocalName());
      assertEquals(expName, elementName);

      elementValue = DOMUtils.getTextContent(statusEl);
      assertEquals("ok", elementValue);
   }
}
