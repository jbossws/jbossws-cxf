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
package org.jboss.test.ws.jaxws.samples.provider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.soap.SOAPBinding;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;
import org.jboss.ws.common.DOMUtils;
import org.w3c.dom.Element;

/**
 * Test a Provider<SOAPMessage>
 *
 * @author Thomas.Diesler@jboss.org
 * @since 29-Jun-2006
 */
public class ProviderMessageTestCase extends JBossWSTest
{
   private String msgString =
      "<soap:Envelope xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/'>" +
      "  <soap:Body>" +
      "    <ns1:somePayload xmlns:ns1='http://org.jboss.ws/provider'/>" +
      "  </soap:Body>" +
      "</soap:Envelope>";
   
   private String msgStringForNullResponse =
      "<soap:Envelope xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/'>" +
      "  <soap:Header>" +
      "    <nsh:returnNullResponse xmlns:nsh='http://org.jboss.ws/foo'/>" +
      "  </soap:Header>" +
      "  <soap:Body>" +
      "    <ns1:somePayload xmlns:ns1='http://org.jboss.ws/provider'/>" +
      "  </soap:Body>" +
      "</soap:Envelope>";

   public static Test suite()
   {
      return new JBossWSTestSetup(ProviderMessageTestCase.class, "jaxws-samples-provider-message.war");
   }

   public void testWSDLAccess() throws Exception
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-samples-provider-message?wsdl");
      Element wsdl = DOMUtils.parse(wsdlURL.openStream());
      assertNotNull(wsdl);
   }

   public void testProviderDispatch() throws Exception
   {
      Dispatch<SOAPMessage> dispatch = createDispatch("ProviderEndpoint");
      SOAPMessage reqMsg = getRequestMessage();

      SOAPMessage resMsg = dispatch.invoke(reqMsg);
      SOAPEnvelope resEnv = resMsg.getSOAPPart().getEnvelope();
      
      SOAPHeader soapHeader = resEnv.getHeader();
      if (soapHeader != null)
         soapHeader.detachNode();
      
      assertEquals(DOMUtils.parse(msgString), resEnv);
   }

   public void testProviderMessage() throws Exception
   {
      SOAPMessage reqMsg = getRequestMessage();
      SOAPEnvelope reqEnv = reqMsg.getSOAPPart().getEnvelope();

      URL epURL = new URL("http://" + getServerHost() + ":8080/jaxws-samples-provider-message");
      SOAPConnection con = SOAPConnectionFactory.newInstance().createConnection();
      SOAPMessage resMsg = con.call(reqMsg, epURL);
      SOAPEnvelope resEnv = resMsg.getSOAPPart().getEnvelope();

      SOAPHeader soapHeader = resEnv.getHeader();
      if (soapHeader != null)
         soapHeader.detachNode();
      
      assertEquals(reqEnv, resEnv);
   }

   public void testProviderMessageNullResponse() throws Exception
   {
      MessageFactory msgFactory = MessageFactory.newInstance();
      SOAPMessage reqMsg = msgFactory.createMessage(null, new ByteArrayInputStream(msgStringForNullResponse.getBytes()));

      URL epURL = new URL("http://" + getServerHost() + ":8080/jaxws-samples-provider-message");
      SOAPConnection con = SOAPConnectionFactory.newInstance().createConnection();
      SOAPMessage resMsg = con.call(reqMsg, epURL);
      if (resMsg != null)
      {
         SOAPPart soapPart = resMsg.getSOAPPart();
         //verify there's either nothing in the reply or at least the response body is empty
         if (soapPart != null && soapPart.getEnvelope() != null && soapPart.getEnvelope().getBody() != null)
         {
            SOAPBody soapBody = soapPart.getEnvelope().getBody();
            assertFalse(soapBody.getChildElements().hasNext());
         }
      }
   }
   
   private SOAPMessage getRequestMessage() throws SOAPException, IOException
   {
      MessageFactory msgFactory = MessageFactory.newInstance();
      SOAPMessage reqMsg = msgFactory.createMessage(null, new ByteArrayInputStream(msgString.getBytes()));
      return reqMsg;
   }

   private Dispatch<SOAPMessage> createDispatch(String target) throws MalformedURLException, JAXBException
   {
      String targetNS = "http://org.jboss.ws/provider";
      QName serviceName = new QName(targetNS, "ProviderService");
      QName portName = new QName(targetNS, "ProviderPort");
      URL endpointAddress = new URL("http://" + getServerHost() + ":8080/jaxws-samples-provider-message/" + target);

      Service service = Service.create(serviceName);
      service.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, endpointAddress.toExternalForm());
      
      Dispatch<SOAPMessage> dispatch = service.createDispatch(portName, SOAPMessage.class, Mode.MESSAGE);
      return dispatch;
   }
}
