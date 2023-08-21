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
package org.jboss.test.ws.jaxws.jbws1815;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import jakarta.xml.soap.Detail;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPConnection;
import jakarta.xml.soap.SOAPConnectionFactory;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.ws.api.util.DOMUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Element;

/**
 * Test case for http://jira.jboss.org/jira/browse/JBWS-1815
 *
 * @author alessio.soldano@jboss.com
 * @since 11-Oct-2007
 */
@RunWith(Arquillian.class)
public class JBWS1815TestCase extends JBossWSTest
{
   private final String msgString =
      "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:my=\"http://www.my-company.it/ws/my-test\">" +
      "  <soapenv:Header/>" +
      "  <soapenv:Body>" +
      "    <my:performTest>" +
      "      <my:Code>43</my:Code>" +
      "    </my:performTest>" +
      "  </soapenv:Body>" +
      "</soapenv:Envelope>";

   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static JavaArchive createDeployments() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws1815.jar");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws1815.ProviderImpl.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1815/META-INF/permissions.xml"), "permissions.xml")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1815/META-INF/wsdl/my-service.wsdl"), "wsdl/my-service.wsdl");
      return archive;
   }

   @Test
   @RunAsClient
   public void testWSDLAccess() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/jaxws-jbws1815/ProviderImpl?wsdl");
      Element wsdl = DOMUtils.parse(wsdlURL.openStream(), getDocumentBuilder());
      assertNotNull(wsdl);
   }

   @Test
   @RunAsClient
   public void testProviderMessage() throws Exception
   {
      SOAPMessage reqMsg = getRequestMessage();
      URL epURL = new URL(baseURL + "/jaxws-jbws1815/ProviderImpl");
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
