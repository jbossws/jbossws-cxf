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
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.rmi.RemoteException;

import javax.activation.DataHandler;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.xml.namespace.QName;
import javax.xml.rpc.Call;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceFactory;
import javax.xml.transform.stream.StreamSource;

import junit.framework.Test;

import org.jboss.ws.common.Constants;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test SOAP with Attachements (SwA) through the JAXRPC DII layer.
 *
 * @author Thomas.Diesler@jboss.org
 * @author <a href="mailto:jason@stacksmash.com">Jason T. Greene</a>
 * @since Nov 16, 2004
 */
public class AttachmentDIITestCase extends JBossWSTest
{
   private final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxrpc-samples-swa";
   private static final String TARGET_NAMESPACE = "http://org.jboss.ws/samples/swa";
   private static final QName SERVICE_NAME = new QName(TARGET_NAMESPACE, "Attachment");

   /** Deploy the test ear */
   public static Test suite() throws Exception
   {
      return new JBossWSTestSetup(AttachmentDIITestCase.class, "jaxrpc-samples-swa.war");
   }
   
   /** Send a multipart message with a text/plain attachment part
    */
   public void testSendMimeImageGIF() throws Exception
   {
      String rpcMethodName = "sendMimeImageGIF";
      Call call = setupMimeMessage(rpcMethodName, "image/gif");

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
         sendAndValidateMimeMessage(call, new DataHandler(url));
      }
   }

   /** Send a multipart message with a text/plain attachment part
    */
   public void testSendMimeImageJPEG() throws Exception
   {
      String rpcMethodName = "sendMimeImageJPEG";
      Call call = setupMimeMessage(rpcMethodName, "image/jpeg");

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
         log.warn("Cannot create Image: " + th);
      }

      if (image != null)
      {
         sendAndValidateMimeMessage(call, new DataHandler(url));
      }
   }

   /** Send a multipart message with a text/plain attachment part
    */
   public void testSendMimeTextPlain() throws Exception
   {
      String rpcMethodName = "sendMimeTextPlain";
      Call call = setupMimeMessage(rpcMethodName, "text/plain");

      URL url = getResourceURL("jaxrpc/samples/swa/attach.txt");
      sendAndValidateMimeMessage(call, new DataHandler(url));
   }

   /** Send a multipart message with a text/plain attachment part
    */
   public void testSendMimeMultipart() throws Exception
   {
      String rpcMethodName = "sendMimeMultipart";
      Call call = setupMimeMessage(rpcMethodName, "multipart/mixed");

      URL url = getResourceURL("jaxrpc/samples/swa/attach.txt");
      MimeMultipart mimepart = new MimeMultipart("mixed");
      MimeBodyPart bodyPart = new MimeBodyPart();
      bodyPart.setDataHandler(new DataHandler(url));
      String bpct = bodyPart.getContentType();
      bodyPart.setHeader("Content-Type", bpct);
      mimepart.addBodyPart(bodyPart);

      sendAndValidateMimeMessage(call, mimepart);
   }

   /** Send a multipart message with a text/plain attachment part
    */
   public void testSendMimeTextXML() throws Exception
   {
      String rpcMethodName = "sendMimeTextXML";
      Call call = setupMimeMessage(rpcMethodName, "text/xml");
      FileInputStream stream = new FileInputStream(getResourceFile("jaxrpc/samples/swa/attach.xml").getPath());
      StreamSource source = new StreamSource(stream);

      sendAndValidateMimeMessage(call, new DataHandler(source, "text/xml"));
   }

   /** Send a multipart message with a text/plain attachment part
    */
   public void testSendMimeApplicationXML() throws Exception
   {
      String rpcMethodName = "sendMimeApplicationXML";
      Call call = setupMimeMessage(rpcMethodName, "application/xml");

      URL url = getResourceURL("jaxrpc/samples/swa/attach.xml");
      sendAndValidateMimeMessage(call, new DataHandler(url));
   }

   /** Setup the multipart/related MIME message
    */
   private Call setupMimeMessage(String rpcMethodName, String contentType)
      throws Exception
   {
      ServiceFactory factory = ServiceFactory.newInstance();
      Service service = factory.createService(SERVICE_NAME);

      Call call = service.createCall();
      call.setOperationName(new QName(TARGET_NAMESPACE, rpcMethodName));
      call.addParameter("message", new QName(Constants.NS_SCHEMA_XSD, "string"), ParameterMode.IN);

      if (contentType.equals("image/jpeg"))
         call.addParameter("mimepart", Constants.TYPE_MIME_IMAGE_JPEG, DataHandler.class, ParameterMode.IN);
      else if (contentType.equals("image/gif"))
         call.addParameter("mimepart", Constants.TYPE_MIME_IMAGE_GIF, DataHandler.class, ParameterMode.IN);
      else if (contentType.equals("text/plain"))
         call.addParameter("mimepart", Constants.TYPE_MIME_TEXT_PLAIN, DataHandler.class, ParameterMode.IN);
      else if (contentType.startsWith("multipart/"))
         call.addParameter("mimepart", Constants.TYPE_MIME_MULTIPART_MIXED, MimeMultipart.class, ParameterMode.IN);
      else if (contentType.equals("text/xml"))
         call.addParameter("mimepart", Constants.TYPE_MIME_TEXT_XML, DataHandler.class, ParameterMode.IN);
      else if (contentType.equals("application/xml"))
         call.addParameter("mimepart", Constants.TYPE_MIME_APPLICATION_XML, DataHandler.class, ParameterMode.IN);

      call.setReturnType(new QName(Constants.NS_SCHEMA_XSD, "string"));

      call.setTargetEndpointAddress(TARGET_ENDPOINT_ADDRESS);

      return call;
   }

   /** Send the message and validate the result
    */
   private void sendAndValidateMimeMessage(Call call, Object mimepart)
      throws RemoteException
   {
      String message = "Some text message";
      String value = (String)call.invoke(new Object[]{message, mimepart});

      assertEquals("[pass]", value);
   }
}
