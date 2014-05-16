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
package org.jboss.test.ws.jaxws.jbws2278;

import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.ws.api.handler.GenericSOAPHandler;

/**
 *
 * @author alessio.soldano@jboss.com
 * @since 30-Sep-2008
 */
public class TestHandler extends GenericSOAPHandler<SOAPMessageContext>
{
   private final String envelopeNamespace;
   private final String contentType;

   public TestHandler(String envelopeNamespace, String contentType)
   {
      super();
      this.envelopeNamespace = envelopeNamespace;
      this.contentType = contentType;
   }

   @Override
   public void close(MessageContext context)
   {
   }

   @Override
   public boolean handleFault(SOAPMessageContext context)
   {
      return handleMessage(context);
   }

   @Override
   public boolean handleInbound(SOAPMessageContext context)
   {
      try
      {
         SOAPMessage soapMessage = context.getMessage();
         soapMessage.saveChanges();
         checkEnvelope(soapMessage);
         checkContentType(soapMessage);
         return true;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   @Override
   public boolean handleOutbound(SOAPMessageContext context)
   {
      try
      {
         SOAPMessage soapMessage = context.getMessage();
         soapMessage.saveChanges();
         checkEnvelope(soapMessage);
         checkContentType(soapMessage);
         return true;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   private void checkEnvelope(SOAPMessage soapMessage) throws SOAPException
   {
      SOAPPart part = soapMessage.getSOAPPart();
      SOAPEnvelope envelope = part.getEnvelope();

      String namespace = envelope.getNamespaceURI();
      if (envelopeNamespace.equals(namespace) == false)
      {
         throw new RuntimeException("Expected '" + envelopeNamespace + "' namespace, actual '" + namespace + "'");
      }
   }

   private void checkContentType(SOAPMessage soapMessage)
   {
      MimeHeaders mimeHeaders = soapMessage.getMimeHeaders();
      String[] ct = mimeHeaders.getHeader("Content-Type");
      boolean found = false;
      if (ct != null)
      {
         for (int i = 0; i < ct.length; i++)
         {
            if (ct[i].startsWith(contentType))
               found = true;
         }
      }
      if (!found)
         throw new RuntimeException("Expected '" + contentType + "' content-type not found in the headers");
   }

}
