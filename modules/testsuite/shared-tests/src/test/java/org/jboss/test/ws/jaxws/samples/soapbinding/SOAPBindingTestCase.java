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
package org.jboss.test.ws.jaxws.samples.soapbinding;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;

import jakarta.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPConnection;
import jakarta.xml.soap.SOAPConnectionFactory;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.Dispatch;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.Service.Mode;

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
 * Test the JSR-181 annotation: jakarta.jws.SOAPBinding
 *
 * @author Thomas.Diesler@jboss.org
 * @author <a href="mailto:jason.greene@jboss.com">Jason T. Greene</a>
 * @since 17-Oct-2005
 */
@RunWith(Arquillian.class)
public class SOAPBindingTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;	
   private final String targetNS = "http://soapbinding.samples.jaxws.ws.test.jboss.org/";


   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-soapbinding.war");
      archive
            .addManifest()
            .addClass(org.jboss.test.ws.jaxws.samples.soapbinding.DocBare.class)
            .addClass(org.jboss.test.ws.jaxws.samples.soapbinding.DocBareServiceImpl.class)
            .addClass(org.jboss.test.ws.jaxws.samples.soapbinding.DocWrapped.class)
            .addClass(org.jboss.test.ws.jaxws.samples.soapbinding.DocWrappedServiceImpl.class)
            .addClass(org.jboss.test.ws.jaxws.samples.soapbinding.ExampleSEI.class)
            .addClass(org.jboss.test.ws.jaxws.samples.soapbinding.ExampleServiceImpl.class)
            .addClass(org.jboss.test.ws.jaxws.samples.soapbinding.SubmitBareRequest.class)
            .addClass(org.jboss.test.ws.jaxws.samples.soapbinding.SubmitBareResponse.class)
            .addClass(org.jboss.test.ws.jaxws.samples.soapbinding.jaxws.SubmitNamespacedPO.class)
            .addClass(org.jboss.test.ws.jaxws.samples.soapbinding.jaxws.SubmitNamespacedPOResponse.class)
            .addClass(org.jboss.test.ws.jaxws.samples.soapbinding.jaxws.SubmitPO.class)
            .addClass(org.jboss.test.ws.jaxws.samples.soapbinding.jaxws.SubmitPOResponse.class)
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/soapbinding/WEB-INF/web.xml"));

      return archive;
   } 

   @Test
   @RunAsClient
   public void testExampleService() throws Exception
   {
      QName serviceName = new QName(targetNS, "ExampleService");
      URL wsdlURL = new URL(baseURL + "/ExampleService?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      ExampleSEI port = service.getPort(ExampleSEI.class);

      Object retObj = port.concat("first", "second", "third");
      assertEquals("first|second|third", retObj);
   }
   @Test
   @RunAsClient
   public void testDocBareService() throws Exception
   {
      QName serviceName = new QName(targetNS, "DocBareService");
      URL wsdlURL = new URL(baseURL + "/DocBareService?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      DocBare port = service.getPort(DocBare.class);

      SubmitBareRequest poReq = new SubmitBareRequest("Ferrari");
      SubmitBareResponse poRes = port.submitPO(poReq);
      assertEquals("Ferrari", poRes.getProduct());
   }
   @Test
   @RunAsClient
   public void testDocBareDispatchService() throws Exception
   {
      QName serviceName = new QName(targetNS, "DocBareService");
      QName portName = new QName(targetNS, "DocBarePort");
      URL wsdlURL = new URL(baseURL + "/DocBareService?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      JAXBContext jbc = JAXBContext.newInstance(new Class[] { SubmitBareRequest.class, SubmitBareResponse.class });
      @SuppressWarnings("rawtypes")
      Dispatch dispatch = service.createDispatch(portName, jbc, Mode.PAYLOAD);

      SubmitBareRequest poReq = new SubmitBareRequest("Ferrari");
      @SuppressWarnings("unchecked")
      SubmitBareResponse poRes = (SubmitBareResponse)dispatch.invoke(poReq);
      assertEquals("Ferrari", poRes.getProduct());
   }
   @Test
   @RunAsClient
   public void testDocBareServiceMessageAccess() throws Exception
   {
      QName serviceName = new QName(targetNS, "DocBareService");
      QName portName = new QName(targetNS, "DocBarePort");
      URL wsdlURL = new URL(baseURL + "/DocBareService?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      Dispatch<SOAPMessage> dispatch = service.createDispatch(portName, SOAPMessage.class, Mode.MESSAGE);

      String reqEnv =
      "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
      " <env:Header/>" +
      " <env:Body>" +
      "  <ns1:SubmitPO xmlns:ns1='" + targetNS + "'>" +
      "   <ns1:product>Ferrari</ns1:product>" +
      "  </ns1:SubmitPO>" +
      " </env:Body>" +
      "</env:Envelope>";

      SOAPMessage reqMsg = MessageFactory.newInstance().createMessage(null, new ByteArrayInputStream(reqEnv.getBytes()));
      SOAPMessage resMsg = dispatch.invoke(reqMsg);

      QName qname = new QName(targetNS, "SubmitPOResponse");
      SOAPElement soapElement = (SOAPElement)resMsg.getSOAPBody().getChildElements(qname).next();
      soapElement = (SOAPElement)soapElement.getChildElements(new QName(targetNS, "product")).next();
      assertEquals("Ferrari", soapElement.getValue());
   }
   @Test
   @RunAsClient
   public void testNamespacedDocBareServiceMessageAccess() throws Exception
   {
      QName serviceName = new QName(targetNS, "DocBareService");
      QName portName = new QName(targetNS, "DocBarePort");
      URL wsdlURL = new URL(baseURL + "/DocBareService?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      Dispatch<SOAPMessage> dispatch = service.createDispatch(portName, SOAPMessage.class, Mode.MESSAGE);

      String requestNamespace = "http://namespace/request";
      String resultNamespace = "http://namespace/result";

      String reqEnv =
      "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
      " <env:Header/>" +
      " <env:Body>" +
      "  <ns1:SubmitNamespacedPO xmlns:ns1='" + requestNamespace+ "'>" +
      "   <ns2:product xmlns:ns2='" + targetNS + "'>Ferrari</ns2:product>" +
      "  </ns1:SubmitNamespacedPO>" +
      " </env:Body>" +
      "</env:Envelope>";

      SOAPMessage reqMsg = MessageFactory.newInstance().createMessage(null, new ByteArrayInputStream(reqEnv.getBytes()));
      SOAPMessage resMsg = dispatch.invoke(reqMsg);

      QName qname = new QName(resultNamespace, "SubmitBareResponse");
      SOAPElement soapElement = (SOAPElement)resMsg.getSOAPBody().getChildElements(qname).next();
      soapElement = (SOAPElement)soapElement.getChildElements(new QName(targetNS, "product")).next();
      assertEquals("Ferrari", soapElement.getValue());
   }
   @Test
   @RunAsClient
   public void testDocWrappedService() throws Exception
   {
      QName serviceName = new QName(targetNS, "DocWrappedService");
      URL wsdlURL = new URL(baseURL + "/DocWrappedService?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      DocWrapped port = service.getPort(DocWrapped.class);

      String poRes = port.submitPO("Ferrari");
      assertEquals("Ferrari", poRes);

      poRes = port.submitNamespacedPO("Ferrari", "message");
      assertEquals("Ferrari", poRes);
   }
   @Test
   @RunAsClient
   public void testDocWrappedServiceMessageAccess() throws Exception
   {
      MessageFactory msgFactory = MessageFactory.newInstance();

      String reqEnv =
      "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
      " <env:Header/>" +
      " <env:Body>" +
      "  <ns1:SubmitPO xmlns:ns1='" + targetNS + "'>" +
      "   <PurchaseOrder>Ferrari</PurchaseOrder>" +
      "  </ns1:SubmitPO>" +
      " </env:Body>" +
      "</env:Envelope>";
      SOAPMessage reqMsg = msgFactory.createMessage(null, new ByteArrayInputStream(reqEnv.getBytes()));

      URL wsdlURL = new URL(baseURL + "/DocWrappedService?wsdl");
      QName serviceName = new QName(targetNS, "DocWrappedService");
      QName portName = new QName(targetNS, "DocWrappedPort");
      Service service = Service.create(wsdlURL, serviceName);
      Dispatch<SOAPMessage> dispatch = service.createDispatch(portName, SOAPMessage.class, Mode.MESSAGE);

      SOAPMessage resMsg = dispatch.invoke(reqMsg);

      QName qname = new QName(targetNS, "SubmitPOResponse");
      SOAPElement soapElement = (SOAPElement)resMsg.getSOAPBody().getChildElements(qname).next();
      soapElement = (SOAPElement)soapElement.getChildElements(new QName("PurchaseOrderAck")).next();
      assertEquals("Ferrari", soapElement.getValue());
   }
   @Test
   @RunAsClient
   public void testNamespacedDocWrappedServiceMessageAccess() throws Exception
   {
      MessageFactory msgFactory = MessageFactory.newInstance();
      SOAPConnection con = SOAPConnectionFactory.newInstance().createConnection();

      String purchaseNamespace = "http://namespace/purchase";
      String resultNamespace = "http://namespace/result";
      String stringNamespace = "http://namespace/string";

      String reqEnv =
      "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
      " <env:Header/>" +
      " <env:Body>" +
      "  <ns1:SubmitNamespacedPO xmlns:ns1='" + targetNS + "'>" +
      "   <ns2:NamespacedPurchaseOrder xmlns:ns2='" + purchaseNamespace + "'>Ferrari</ns2:NamespacedPurchaseOrder>" +
      "   <ns3:NamespacedString xmlns:ns3='" + stringNamespace + "'>message</ns3:NamespacedString>" +
      "  </ns1:SubmitNamespacedPO>" +
      " </env:Body>" +
      "</env:Envelope>";
      SOAPMessage reqMsg = msgFactory.createMessage(null, new ByteArrayInputStream(reqEnv.getBytes()));
      URL epURL = new URL(baseURL + "/DocWrappedService");

      SOAPMessage resMsg = con.call(reqMsg, epURL);

      QName qname = new QName(targetNS, "SubmitNamespacedPOResponse");
      SOAPElement soapElement = (SOAPElement)resMsg.getSOAPBody().getChildElements(qname).next();
      soapElement = (SOAPElement)soapElement.getChildElements(new QName(resultNamespace, "NamespacedPurchaseOrderAck")).next();
      assertEquals("Ferrari", soapElement.getValue());
   }
}
