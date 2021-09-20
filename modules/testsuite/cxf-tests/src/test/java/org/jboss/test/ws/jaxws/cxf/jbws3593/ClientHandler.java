/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.jbws3593;

import java.util.List;
import java.util.Map;

import jakarta.mail.internet.ContentType;
import jakarta.mail.internet.ParseException;
import jakarta.xml.soap.MimeHeaders;
import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.logging.Logger;
import org.jboss.ws.api.handler.GenericSOAPHandler;

public class ClientHandler extends GenericSOAPHandler<SOAPMessageContext>
{
   private static Logger log = Logger.getLogger(ClientHandler.class);

   private boolean checkMtom;

   public ClientHandler(boolean checkMtom) {
      super();
      this.checkMtom = checkMtom;
   }

   public boolean handleInbound(SOAPMessageContext msgContext)
   {
      log.info("handleInbound");

      try
      {
         SOAPEnvelope soapEnvelope = (SOAPEnvelope)msgContext.getMessage().getSOAPPart().getEnvelope();
         String nsURI = soapEnvelope.getNamespaceURI();

         log.info("nsURI=" + nsURI);

         if (!SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE.equals(nsURI))
         {
            throw new RuntimeException("Wrong NS uri: " + nsURI);
         }
      }
      catch (SOAPException se)
      {
         throw new WebServiceException(se);
      }

      ContentType contentType = getContentType(msgContext);

      if (contentType != null)
      {
         log.info("contentType="+contentType);
         String startInfo = contentType.getParameter("start-info");
         if (!checkMtom) {
            if (startInfo != null) {
               throw new RuntimeException("Unexpected multipart/related message!");
            } else {
               return true;
            }
         }
         if (!startInfo.equals(SOAPConstants.SOAP_1_1_CONTENT_TYPE))
         {
            throw new RuntimeException("Wrong start info: " + startInfo);
         }
      }
      else
      {
         throw new RuntimeException("Missing content type");
      }

      return true;
   }

   protected ContentType getContentType(SOAPMessageContext msgContext)
   {
      ContentType contentType = null;

      try
      {
         @SuppressWarnings("unchecked")
         Map<String, List<String>> headers = (Map<String, List<String>>)msgContext.get(MessageContext.HTTP_RESPONSE_HEADERS);
         List<String> ctype = (headers == null) ? null : headers.get("Content-Type");
         if (ctype == null)
         {
            //Cxf stores it in lower case
            ctype = (headers == null) ? null : headers.get("content-type");
         }
         log.info("ctype="+ctype);

         if (ctype == null)
         {
            //Native has already processed this header into the message
            SOAPMessage soapMessage = msgContext.getMessage();
            MimeHeaders mimeHeaders = soapMessage.getMimeHeaders();
            String[] ct = mimeHeaders.getHeader("Content-Type");
            log.info("ct="+ct);
            if (ct != null)
            {
               contentType = new ContentType(ct[0]);
            }
         }
         else
         {
            contentType = new ContentType(ctype.get(0));
         }
      }
      catch (ParseException e)
      {
         throw new WebServiceException(e);
      }

      return contentType;
   }
}
