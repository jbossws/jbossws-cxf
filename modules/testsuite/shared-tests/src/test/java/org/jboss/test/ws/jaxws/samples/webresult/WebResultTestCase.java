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
package org.jboss.test.ws.jaxws.samples.webresult;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPConnection;
import jakarta.xml.soap.SOAPConnectionFactory;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.Service;

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
 * Test the JSR-181 annotation: jakarta.jws.webresult
 *
 * @author Thomas.Diesler@jboss.org
 * @since 07-Oct-2005
 */
@RunWith(Arquillian.class)
public class WebResultTestCase extends JBossWSTest
{
   private String targetNS = "http://webresult.samples.jaxws.ws.test.jboss.org/";
   
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-webresult.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.samples.webresult.CustomerRecord.class)
               .addClass(org.jboss.test.ws.jaxws.samples.webresult.CustomerServiceImpl.class)
               .addClass(org.jboss.test.ws.jaxws.samples.webresult.USAddress.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/webresult/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testLocateCustomer() throws Exception
   {
      QName serviceName = new QName(targetNS, "CustomerServiceService");
      URL wsdlURL = getResourceURL("jaxws/samples/webresult/META-INF/wsdl/CustomerService.wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      CustomerService port = service.getPort(CustomerService.class);
      
      USAddress addr = new USAddress();
      addr.setAddress("Wall Street");

      CustomerRecord retObj = port.locateCustomer("Mickey", "Mouse", addr);
      assertEquals("Mickey", retObj.getFirstName());
      assertEquals("Mouse", retObj.getLastName());
      assertEquals("Wall Street", retObj.getAddress().getAddress());
   }

   @Test
   @RunAsClient
   public void testMessageAccess() throws Exception
   {
      MessageFactory msgFactory = MessageFactory.newInstance();
      SOAPConnection con = SOAPConnectionFactory.newInstance().createConnection();

      String reqEnv =
      "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>" +
      " <env:Header/>" +
      " <env:Body>" +
      "  <ns1:locateCustomer xmlns:ns1='" + targetNS + "'>" +
      "   <FirstName>Mickey</FirstName>" +
      "   <LastName>Mouse</LastName>" +
      "   <Address>" +
      "     <address>Wall Street</address>" +
      "   </Address>" +
      "  </ns1:locateCustomer>" +
      " </env:Body>" +
      "</env:Envelope>";
      SOAPMessage reqMsg = msgFactory.createMessage(null, new ByteArrayInputStream(reqEnv.getBytes()));

      URL epURL = baseURL;

      SOAPMessage resMsg = con.call(reqMsg, epURL);

      QName qname = new QName(targetNS, "locateCustomerResponse");
      SOAPElement soapElement = (SOAPElement)resMsg.getSOAPBody().getChildElements(qname).next();
      soapElement = (SOAPElement)soapElement.getChildElements(new QName("CustomerRecord")).next();
      assertNotNull("Expected CustomerRecord", soapElement);
   }
}
