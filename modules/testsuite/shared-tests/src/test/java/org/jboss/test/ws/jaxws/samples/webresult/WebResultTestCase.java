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
package org.jboss.test.ws.jaxws.samples.webresult;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Service;

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
 * Test the JSR-181 annotation: javax.jws.webresult
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
