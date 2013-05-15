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
import java.rmi.RemoteException;
import java.util.Iterator;

import javax.mail.BodyPart;
import javax.mail.internet.MimeMultipart;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.handler.soap.SOAPMessageContext;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;

/**
 * Service Endpoint for the MIME mapping required by JAXRPC-1.1
 *
 * image/gif         java.awt.Image
 * image/jpeg        java.awt.Image
 * text/plain        java.lang.String
 * multipart/*       javax.mail.internet.MimeMultipart
 * text/xml          javax.xml.transform.Source
 * application/xml   javax.xml.transform.Source
 *
 * @author Thomas.Diesler@jboss.org
 * @since Nov 17, 2004
 */
public class AttachmentImpl implements Attachment, ServiceLifecycle
{
   private ServletEndpointContext context;

   /** Service endpoint method for image/gif
    */
   public String sendMimeImageGIF(String message, Object mimepart) throws RemoteException
   {
      StringBuffer buffer = new StringBuffer();

      validateStringMessage(buffer, message);

      String expContentType = "image/gif";
      validateAttachmentPart(buffer, expContentType, mimepart);

      String resultStr = getResultStr(buffer, expContentType);
      return resultStr;
   }

   /** Service endpoint method for image/jpeg
    */
   public String sendMimeImageJPEG(String message, Image mimepart) throws RemoteException
   {
      StringBuffer buffer = new StringBuffer();

      validateStringMessage(buffer, message);

      String expContentType = "image/jpeg";
      validateAttachmentPart(buffer, expContentType, mimepart);

      String resultStr = getResultStr(buffer, expContentType);
      return resultStr;
   }

   /** Service endpoint method for text/plain
    */
   public String sendMimeTextPlain(String message, String mimepart) throws RemoteException
   {
      StringBuffer buffer = new StringBuffer();

      validateStringMessage(buffer, message);

      String expContentType = "text/plain";
      validateAttachmentPart(buffer, expContentType, mimepart);

      String resultStr = getResultStr(buffer, expContentType);
      return resultStr;
   }

   /** Service endpoint method for multipart/*
    */
   public String sendMimeMultipart(String message, MimeMultipart mimepart) throws RemoteException
   {
      StringBuffer buffer = new StringBuffer();

      validateStringMessage(buffer, message);

      String expContentType = "multipart/*";
      validateAttachmentPart(buffer, expContentType, mimepart);

      String resultStr = getResultStr(buffer, expContentType);
      return resultStr;
   }

   /** Service endpoint method for text/xml
    */
   public String sendMimeTextXML(String message, Object mimepart) throws RemoteException
   {
      StringBuffer buffer = new StringBuffer();

      validateStringMessage(buffer, message);

      String expContentType = "text/xml";
      validateAttachmentPart(buffer, expContentType, mimepart);

      String resultStr = getResultStr(buffer, expContentType);
      return resultStr;
   }

   /** Service endpoint method for application/xml
    */
   public String sendMimeApplicationXML(String message, Source mimepart) throws RemoteException
   {
      StringBuffer buffer = new StringBuffer();

      validateStringMessage(buffer, message);

      String expContentType = "application/xml";
      validateAttachmentPart(buffer, expContentType, mimepart);

      String resultStr = getResultStr(buffer, expContentType);
      return resultStr;
   }

   private void validateStringMessage(StringBuffer buffer, String message)
   {
      if ("Some text message".equals(message) == false)
         buffer.append("[message=" + message + "]");
   }

   private void validateAttachmentPart(StringBuffer buffer, String expContentType, Object mimepart)
   {
      SOAPMessageContext msgContext = (SOAPMessageContext)context.getMessageContext();
      SOAPMessage soapMessage = msgContext.getMessage();

      Iterator attachments = soapMessage.getAttachments();
      if (attachments.hasNext())
      {
         AttachmentPart ap = (AttachmentPart)attachments.next();
         String contentType = ap.getContentType();

         if (expContentType.equals("multipart/*"))
         {
            if (contentType.startsWith("multipart/") == false)
               buffer.append("[contentType=" + contentType + "]");
         }
         else if (expContentType.equals("text/xml"))
         {
            if (contentType.equals("text/xml") == false && contentType.equals("application/xml") == false)
               buffer.append("[contentType=" + contentType + "]");
         }
         else
         {
            if (contentType.equals(expContentType) == false)
               buffer.append("[contentType=" + contentType + "]");
         }
         validateSinglePart(buffer, expContentType, ap);
      }
      else
      {
         buffer.append("[no attachments]");
      }

      validateSinglePart(buffer, expContentType, mimepart);
   }

   private void validateSinglePart(StringBuffer buffer, String contentType, Object content)
   {
      try
      {
         if (content instanceof AttachmentPart)
            content = ((AttachmentPart)content).getContent();
         
         if (contentType.equals("image/gif") || contentType.equals("image/jpeg"))
         {
            if ((content instanceof Image) == false)
               buffer.append("[content=" + content + "]");
         }
         else if (contentType.equals("text/plain"))
         {
            if ((content instanceof String) == false)
               buffer.append("[content=" + content + "]");
         }
         else if (contentType.startsWith("multipart/"))
         {
            if ((content instanceof MimeMultipart) == false)
            {
               buffer.append("[content=" + content + "]");
            }
            else
            {
               MimeMultipart mmp = (MimeMultipart)content;

               int mmpCount = mmp.getCount();
               if (mmpCount < 1)
                  buffer.append("[count=" + mmpCount + "]");

               for (int i = 0; i < mmpCount; i++)
               {
                  BodyPart bp = mmp.getBodyPart(i);
                  String bpct = bp.getContentType();
                  Object bpc = bp.getContent();
                  validateSinglePart(buffer, bpct, bpc);
               }
            }
         }
         else if (contentType.equals("text/xml") || contentType.equals("application/xml"))
         {
            if ((content instanceof Source) == false)
               buffer.append("[content=" + content + "]");
         }
         else
         {
            throw new IllegalArgumentException("Unsupported mime type: " + contentType);
         }
      }
      catch (Exception e)
      {
         buffer.append("[" + e + "]");
      }
   }

   private String getResultStr(StringBuffer buffer, String expContentType)
   {
      String retStr = (buffer.length() == 0 ? "[pass]" : buffer.toString());
      System.out.println(expContentType + ": " + retStr);
      return retStr;
   }

   // ServiceLifecycle *******************************************************************************************

   public void init(Object context) throws ServiceException
   {
      this.context = (ServletEndpointContext)context;
   }

   public void destroy()
   {
      this.context = null;
   }
}
