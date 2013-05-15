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
package org.jboss.test.ws.jaxws.jbws1815;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.Detail;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import junit.framework.Test;

import org.jboss.ws.api.util.DOMUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;
import org.w3c.dom.Element;

/**
 * Test case for http://jira.jboss.org/jira/browse/JBWS-1815
 *
 * @author alessio.soldano@jboss.com
 * @since 11-Oct-2007
 */
public class JBWS1815TestCase extends JBossWSTest
{
   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxws-jbws1815/ProviderImpl";

   private String msgString =
      "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:my=\"http://www.my-company.it/ws/my-test\">" +
      "  <soapenv:Header/>" +
      "  <soapenv:Body>" +
      "    <my:performTest>" +
      "      <my:Code>43</my:Code>" +
      "    </my:performTest>" +
      "  </soapenv:Body>" +
      "</soapenv:Envelope>";

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS1815TestCase.class, "jaxws-jbws1815.jar");
   }

   public void testWSDLAccess() throws Exception
   {
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
      Element wsdl = DOMUtils.parse(wsdlURL.openStream(), getDocumentBuilder());
      assertNotNull(wsdl);
   }

   public void testProviderMessage() throws Exception
   {
      SOAPMessage reqMsg = getRequestMessage();
      URL epURL = new URL(TARGET_ENDPOINT_ADDRESS);
      SOAPConnection con = SOAPConnectionFactory.newInstance().createConnection();
      SOAPMessage resMsg = con.call(reqMsg, epURL);
      SOAPEnvelope resEnv = resMsg.getSOAPPart().getEnvelope();
      Detail detail = resEnv.getBody().getFault().getDetail();
      assertNotNull(detail);
      SOAPElement exception = (SOAPElement)detail.getDetailEntries().next();
      assertNotNull(exception);
      assertEquals("MyWSException", exception.getElementQName().getLocalPart());
      assertEquals("http://www.my-company.it/ws/my-test", exception.getElementQName().getNamespaceURI());
      SOAPElement message = (SOAPElement)exception.getChildElements().next();
      assertNotNull(message);
      assertEquals("message", message.getNodeName());
      assertEquals("This is a faked error", message.getValue());
   }

   private SOAPMessage getRequestMessage() throws SOAPException, IOException
   {
      MessageFactory msgFactory = MessageFactory.newInstance();
      SOAPMessage reqMsg = msgFactory.createMessage(null, new ByteArrayInputStream(msgString.getBytes()));
      return reqMsg;
   }
   
   private DocumentBuilder getDocumentBuilder()
   {
      DocumentBuilderFactory factory = null;
      try
      {
         factory = DocumentBuilderFactory.newInstance();
         factory.setValidating(false);
         factory.setNamespaceAware(true);
         factory.setExpandEntityReferences(false);
         factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
         DocumentBuilder builder = factory.newDocumentBuilder();
         return builder;
      }
      catch (Exception e)
      {
         throw new RuntimeException("Unable to create document builder", e);
      }
   }

}
