/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
