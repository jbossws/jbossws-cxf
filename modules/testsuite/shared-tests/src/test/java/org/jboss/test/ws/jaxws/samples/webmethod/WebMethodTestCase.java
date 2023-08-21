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
package org.jboss.test.ws.jaxws.samples.webmethod;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringReader;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPConnection;
import jakarta.xml.soap.SOAPConnectionFactory;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPFault;
import jakarta.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import jakarta.xml.ws.Dispatch;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.Service.Mode;
import jakarta.xml.ws.soap.SOAPFaultException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test the JSR-181 annotation: jakarta.jws.webmethod
 *
 * @author Thomas.Diesler@jboss.org
 * @since 07-Oct-2005
 */
@RunWith(Arquillian.class)
public class WebMethodTestCase extends JBossWSTest
{
   private final String targetNS = "http://webmethod.samples.jaxws.ws.test.jboss.org/";

   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-webmethod.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.samples.webmethod.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.samples.webmethod.EndpointImpl.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/webmethod/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testLegalAccess() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/TestService?wsdl");
      QName serviceName = new QName(targetNS, "EndpointService");

      Service service = Service.create(wsdlURL, serviceName);
      Endpoint port = service.getPort(Endpoint.class);

      Object retObj = port.echo("Hello");
      assertEquals("Hello", retObj);
   }

   @Test
   @RunAsClient
   public void testLegalMessageAccess() throws Exception
   {
      MessageFactory msgFactory = MessageFactory.newInstance();
      SOAPConnection con = SOAPConnectionFactory.newInstance().createConnection();

      String reqEnv =
         "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
         " <env:Header/>" +
         " <env:Body>" +
         "  <ns1:echoString xmlns:ns1='" + targetNS + "'>" +
         "   <arg0>Hello</arg0>" +
         "  </ns1:echoString>" +
         " </env:Body>" +
         "</env:Envelope>";
      SOAPMessage reqMsg = msgFactory.createMessage(null, new ByteArrayInputStream(reqEnv.getBytes()));

      URL epURL = new URL(baseURL + "/TestService");
      SOAPMessage resMsg = con.call(reqMsg, epURL);

      QName qname = new QName(targetNS, "echoStringResponse");
      SOAPElement soapElement = (SOAPElement)resMsg.getSOAPBody().getChildElements(qname).next();
      soapElement = (SOAPElement)soapElement.getChildElements(new QName("return")).next();
      assertEquals("Hello", soapElement.getValue());
   }

   @Test
   @RunAsClient
   public void testIllegalMessageAccess() throws Exception
   {
      MessageFactory msgFactory = MessageFactory.newInstance();
      SOAPConnection con = SOAPConnectionFactory.newInstance().createConnection();

      String reqEnv =
         "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
         " <env:Header/>" +
         " <env:Body>" +
         "  <ns1:noWebMethod xmlns:ns1='" + targetNS + "'>" +
         "   <String_1>Hello</String_1>" +
         "  </ns1:noWebMethod>" +
         " </env:Body>" +
         "</env:Envelope>";
      SOAPMessage reqMsg = msgFactory.createMessage(null, new ByteArrayInputStream(reqEnv.getBytes()));

      URL epURL = new URL(baseURL + "/TestService");
      SOAPMessage resMsg = con.call(reqMsg, epURL);
      SOAPFault soapFault = resMsg.getSOAPBody().getFault();
      assertNotNull("Expected SOAPFault", soapFault);

      String faultString = soapFault.getFaultString();
      assertTrue(faultString, faultString.indexOf("noWebMethod") > 0);
   }

   @Test
   @RunAsClient
   public void testIllegalDispatchAccess() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/TestService?wsdl");
      QName serviceName = new QName(targetNS, "EndpointService");
      QName portName = new QName(targetNS, "EndpointPort");

      String reqPayload =
         "<ns1:noWebMethod xmlns:ns1='" + targetNS + "'>" +
         " <String_1>Hello</String_1>" +
         "</ns1:noWebMethod>";

      Service service = Service.create(wsdlURL, serviceName);
      Dispatch<Source> dispatch = service.createDispatch(portName, Source.class, Mode.PAYLOAD);
      try
      {
         dispatch.invoke(new StreamSource(new StringReader(reqPayload)));
         fail("SOAPFaultException expected");
      }
      catch (SOAPFaultException ex)
      {
         // ignore
      }
   }
}
