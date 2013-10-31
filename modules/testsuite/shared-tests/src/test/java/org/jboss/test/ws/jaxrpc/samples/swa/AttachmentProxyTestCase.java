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
package org.jboss.test.ws.jaxrpc.samples.swa;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.FileInputStream;
import java.net.URL;

import javax.activation.DataHandler;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceFactory;
import javax.xml.transform.stream.StreamSource;

import junit.framework.Test;

import org.jboss.wsf.test.CleanupOperation;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test SOAP with Attachements (SwA) through the JAXRPC dynamic proxy layer.
 *
 * @author Thomas.Diesler@jboss.org
 * @author <a href="mailto:jason@stacksmash.com">Jason T. Greene</a>
 * @since 16-Nov-2004
 */
public class AttachmentProxyTestCase extends JBossWSTest
{
   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxrpc-samples-swa";
   private static final String TARGET_NAMESPACE = "http://org.jboss.ws/samples/swa";
   private static Attachment port;

   /** Deploy the test ear */
   public static Test suite() throws Exception
   {
      return new JBossWSTestSetup(AttachmentProxyTestCase.class, "jaxrpc-samples-swa.war, jaxrpc-samples-swa-client.jar", new CleanupOperation() {
         @Override
         public void cleanUp() {
            port = null;
         }
      });
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      if (port == null)
      {
         port = getService(Attachment.class, "Attachment", "AttachmentPort");
      }
   }

   @SuppressWarnings("unchecked")
   protected <T> T getService(final Class<T> clazz, final String serviceName, final String portName) throws Exception {
      ServiceFactory serviceFactory = ServiceFactory.newInstance();
      Service service = serviceFactory.createService(new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl"), new QName(TARGET_NAMESPACE, serviceName));
      return (T) service.getPort(new QName(TARGET_NAMESPACE, portName), clazz);
   }

   /** Send a multipart message with a text/plain attachment part
    */
   public void testSendMimeImageGIF() throws Exception
   {
      URL url = getResourceURL("jaxrpc/samples/swa/attach.gif");

      // On Linux the X11 server must be installed properly to create images successfully.
      // If the image cannot be created in the test VM, we assume it cannot be done on the
      // server either, so we just skip the test
      Image image = null;
      try
      {
         image = Toolkit.getDefaultToolkit().createImage(url);
      }
      catch (Throwable th)
      {
         //log.warn("Cannot create Image: " + th);
      }

      if (image != null)
      {
         String value = port.sendMimeImageGIF("Some text message", new DataHandler(url));
         assertEquals("[pass]", value);
      }
   }

   /** Send a multipart message with a text/plain attachment part
    */
   public void testSendMimeImageJPEG() throws Exception
   {
      URL url = getResourceURL("jaxrpc/samples/swa/attach.jpeg");

      // On Linux the X11 server must be installed properly to create images successfully.
      // If the image cannot be created in the test VM, we assume it cannot be done on the
      // server either, so we just skip the test
      Image image = null;
      try
      {
         image = Toolkit.getDefaultToolkit().createImage(url);
      }
      catch (Throwable th)
      {
         //log.warn("Cannot create Image: " + th);
      }

      if (image != null)
      {
         String value = port.sendMimeImageJPEG("Some text message", image);
         assertEquals("[pass]", value);
      }
   }

   /** Send a multipart message with a text/plain attachment part
    */
   public void testSendMimeTextPlain() throws Exception
   {
      String value = port.sendMimeTextPlain("Some text message", "This is a plain text attachment.");
      assertEquals("[pass]", value);
   }

   /** Send a multipart message with a text/plain attachment part
    */
   public void testSendMimeMultipart() throws Exception
   {
      URL url = getResourceURL("jaxrpc/samples/swa/attach.txt");
      MimeMultipart multipart = new MimeMultipart("mixed");
      MimeBodyPart bodyPart = new MimeBodyPart();
      bodyPart.setDataHandler(new DataHandler(url));
      String bpct = bodyPart.getContentType();
      bodyPart.setHeader("Content-Type", bpct);
      multipart.addBodyPart(bodyPart);

      String value = port.sendMimeMultipart("Some text message", multipart);
      assertEquals("[pass]", value);
   }

   /** Send a multipart message with a text/plain attachment part
    */
   public void testSendMimeTextXML() throws Exception
   {
      FileInputStream stream = new FileInputStream(getResourceFile("jaxrpc/samples/swa/attach.xml").getPath());
      StreamSource source = new StreamSource(stream);

      String value = port.sendMimeTextXML("Some text message", new DataHandler(source, "text/xml"));
      assertEquals("[pass]", value);
   }

   /** Send a multipart message with a text/plain attachment part
    */
   public void testSendMimeApplicationXML() throws Exception
   {
      FileInputStream stream = new FileInputStream(getResourceFile("jaxrpc/samples/swa/attach.xml").getPath());
      StreamSource source = new StreamSource(stream);

      String value = port.sendMimeApplicationXML("Some text message", source);
      assertEquals("[pass]", value);
   }
}
