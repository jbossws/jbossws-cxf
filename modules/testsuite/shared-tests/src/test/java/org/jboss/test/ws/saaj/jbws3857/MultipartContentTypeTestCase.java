/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.saaj.jbws3857;

import static org.jboss.wsf.test.JBossWSTestHelper.getTestResourcesDir;

import java.io.File;
import java.net.URL;

import javax.activation.DataHandler;
import javax.activation.URLDataSource;
import javax.xml.namespace.QName;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPMessage;

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
