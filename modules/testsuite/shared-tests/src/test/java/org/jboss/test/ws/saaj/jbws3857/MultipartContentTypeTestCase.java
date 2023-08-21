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
package org.jboss.test.ws.saaj.jbws3857;

import static org.jboss.wsf.test.JBossWSTestHelper.getTestResourcesDir;

import java.io.File;
import java.net.URL;

import jakarta.activation.DataHandler;
import jakarta.activation.URLDataSource;
import javax.xml.namespace.QName;
import jakarta.xml.soap.AttachmentPart;
import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPBodyElement;
import jakarta.xml.soap.SOAPConnection;
import jakarta.xml.soap.SOAPConnectionFactory;
import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPMessage;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
@RunWith(Arquillian.class)
public class MultipartContentTypeTestCase extends JBossWSTest
{
   private static final String PROJECT_NAME = "reproducer-eap-wrong-multipart";
   private static final String IN_IMG_NAME = "test.png";
   @ArquillianResource
   private URL baseURL;
   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws3857.war");
         archive
            .addManifest()
            .addClass(org.jboss.test.ws.saaj.jbws3857.SoapMultipartCheckerServlet.class)
            .addAsWebInfResource(new File(getTestResourcesDir() + "/saaj/jbws3857/META-INF/beans.xml"), "classes/META-INF/beans.xml")
            .addAsWebInfResource(new File(getTestResourcesDir() + "/saaj/jbws3857/test.png"), "classes/test.png")
            .addAsWebInfResource(new File(getTestResourcesDir() + "/saaj/jbws3857/WEB-INF/jboss-web.xml"), "jboss-web.xml")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/saaj/jbws3857/WEB-INF/web.xml"));
      return archive;
   }
  
   @Test
   @RunAsClient
   public void testSendMultipartSoapMessage() throws Exception {
      final MessageFactory msgFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
      final SOAPMessage msg = msgFactory.createMessage();
      final SOAPBodyElement bodyElement = msg.getSOAPBody().addBodyElement(
         new QName("urn:ledegen:soap-attachment:1.0", "echoImage"));
      bodyElement.addTextNode("cid:" + IN_IMG_NAME);

      final AttachmentPart ap = msg.createAttachmentPart();
      ap.setDataHandler(getResource("saaj/jbws3857/" + IN_IMG_NAME));
      ap.setContentId(IN_IMG_NAME);
      msg.addAttachmentPart(ap);

      final SOAPConnectionFactory conFactory = SOAPConnectionFactory.newInstance();
      final SOAPConnection connection = conFactory.createConnection();
      final SOAPMessage response = connection.call(msg, new URL("http://" + baseURL.getHost()+ ":" + baseURL.getPort() + "/" + PROJECT_NAME + "/testServlet"));

      final String contentTypeWeHaveSent = getBodyElementTextValue(response);
      assertContentTypeStarts("multipart/related", contentTypeWeHaveSent);
   }

   private void assertContentTypeStarts(final String expectedStart, final String actual) {
      if (!actual.startsWith(expectedStart)) {
         Assert.fail("We have send request with Content-Type " + actual + ", but expected start is " + expectedStart);
      }
   }

   private String getBodyElementTextValue(final SOAPMessage msg) throws Exception {
      final SOAPBodyElement bodyElement = (SOAPBodyElement) msg.getSOAPBody().getChildElements().next();
      final String result = bodyElement.getTextContent();
      return result;
   }

   private DataHandler getResource(final String resource) throws Exception {
      URL imageUrl = getResourceURL(resource);
      return new DataHandler(new URLDataSource(imageUrl));
   }

}
