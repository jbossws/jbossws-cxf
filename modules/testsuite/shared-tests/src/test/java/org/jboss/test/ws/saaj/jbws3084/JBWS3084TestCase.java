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
package org.jboss.test.ws.saaj.jbws3084;

import static org.jboss.wsf.test.JBossWSTestHelper.getTestResourcesDir;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;

import javax.xml.namespace.QName;
import jakarta.xml.soap.AttachmentPart;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPConnection;
import jakarta.xml.soap.SOAPConnectionFactory;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPFactory;
import jakarta.xml.soap.SOAPMessage;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-3084] Enable control of chunked encoding when using SOAPConnection.
 *
 * @author sberyozk@redhat.com
 * @author alessio.soldano@jboss.com
 */
@RunWith(Arquillian.class)
public class JBWS3084TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "saaj-soap-connection.war");
         archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client\n"))
               .addClass(ServiceIface.class)
               .addClass(ServiceImpl.class)
               .addClass(InputStreamDataSource.class)
               .addAsWebInfResource(new File(getTestResourcesDir() + "/saaj/jbws3084/WEB-INF/wsdl/SaajService.wsdl"), "wsdl/SaajService.wsdl")
               .setWebXML(new File(getTestResourcesDir() + "/saaj/jbws3084/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testSoapConnectionPostWithoutChunkedEncoding() throws Exception
   {
      doTestSoapConnection(true);
   }

   @Test
   @RunAsClient
   public void testSoapConnectionPostWithChunkedEncoding() throws Exception
   {
      doTestSoapConnection(false);
   }

   private void doTestSoapConnection(boolean disableChunking) throws Exception
   {
      SOAPFactory soapFac = SOAPFactory.newInstance();
      MessageFactory msgFac = MessageFactory.newInstance();
      SOAPConnectionFactory conFac = SOAPConnectionFactory.newInstance();
      SOAPMessage msg = msgFac.createMessage();

      if (disableChunking)
      {
         // this is the custom header checked by ServiceImpl
         msg.getMimeHeaders().addHeader("Transfer-Encoding-Disabled", "true");
         // this is a hint to SOAPConnection that the chunked encoding is not needed
         msg.getMimeHeaders().addHeader("Transfer-Encoding", "disabled");
      }

      QName sayHi = new QName("http://www.jboss.org/jbossws/saaj", "sayHello");
      msg.getSOAPBody().addChildElement(soapFac.createElement(sayHi));
      AttachmentPart ap1 = msg.createAttachmentPart();

      char[] content = new char[16 * 1024];
      Arrays.fill(content, 'A');

      ap1.setContent(new String(content), "text/plain");
      msg.addAttachmentPart(ap1);

      AttachmentPart ap2 = msg.createAttachmentPart();
      ap2.setContent("Attachment content - Part 2", "text/plain");
      msg.addAttachmentPart(ap2);
      msg.saveChanges();

      SOAPConnection con = conFac.createConnection();

      final String serviceURL = baseURL.toString();

      URL endpoint = new URL(serviceURL);
      SOAPMessage response = con.call(msg, endpoint);
      QName sayHiResp = new QName("http://www.jboss.org/jbossws/saaj", "sayHelloResponse");

      Iterator<?> sayHiRespIterator = response.getSOAPBody().getChildElements(sayHiResp);
      SOAPElement soapElement = (SOAPElement) sayHiRespIterator.next();
      assertNotNull(soapElement);

      assertEquals(2, response.countAttachments());

      String[] values = response.getMimeHeaders().getHeader("Transfer-Encoding-Disabled");
      if (disableChunking)
      {
         // this means that the ServiceImpl executed the code branch verifying 
         // that chunking was disabled 
         assertNotNull(values);
         assertTrue(values.length == 1);
      }
      else
      {
         assertNull(values);
      }
   }
}
