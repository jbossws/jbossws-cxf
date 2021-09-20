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
package org.jboss.test.ws.jaxws.jbws2419;

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

/**
 * A SOAP 1.2 client side handler
 *
 * @author mageshbk@jboss.com
 * @since 20-Feb-2009
 */
public class SOAP12ClientHandler extends GenericSOAPHandler<SOAPMessageContext>
{
   private static Logger log = Logger.getLogger(SOAP12ClientHandler.class);

   @Override
   public boolean handleInbound(SOAPMessageContext msgContext)
   {
      log.info("handleInbound");

      try
      {
         SOAPEnvelope soapEnvelope = msgContext.getMessage().getSOAPPart().getEnvelope();
         String nsURI = soapEnvelope.getNamespaceURI();

         log.info("nsURI=" + nsURI);

         if (!SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE.equals(nsURI))
         {
            return false;
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
         if (!startInfo.equals(SOAPConstants.SOAP_1_2_CONTENT_TYPE))
         {
            return false;
         }
      }
      else
      {
         return false;
      }

      return true;
   }

   protected ContentType getContentType(MessageContext msgContext)
   {
      ContentType contentType = null;

      try
      {
         //Metro does not process this header into the message
         @SuppressWarnings("unchecked")
         Map<String, List<String>> headers = (Map<String, List<String>>)msgContext.get(MessageContext.HTTP_REQUEST_HEADERS);
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
            SOAPMessage soapMessage = ((SOAPMessageContext)msgContext).getMessage();
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
