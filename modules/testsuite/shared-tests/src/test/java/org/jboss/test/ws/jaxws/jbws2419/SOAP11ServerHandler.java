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

import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.logging.Logger;
import org.jboss.ws.api.handler.GenericSOAPHandler;

/**
 * A SOAP 1.1 server side handler
 *
 * @author mageshbk@jboss.com
 * @since 20-Feb-2009
 */
public class SOAP11ServerHandler extends GenericSOAPHandler<LogicalMessageContext>
{
   private static Logger log = Logger.getLogger(SOAP11ServerHandler.class);

   @Override
   public boolean handleInbound(MessageContext msgContext)
   {
      log.info("handleInbound");

      ContentType contentType = getContentType(msgContext);

      if (contentType != null)
      {
         log.info("contentType="+contentType);
         String startInfo = contentType.getParameter("start-info");
         if (!startInfo.equals(SOAPConstants.SOAP_1_1_CONTENT_TYPE))
         {
            return false;
         }
      }
      else
      {
         return false;
      }
      try
      {
         SOAPEnvelope soapEnvelope = ((SOAPMessageContext)msgContext).getMessage().getSOAPPart().getEnvelope();
         String nsURI = soapEnvelope.getNamespaceURI();

         log.info("nsURI=" + nsURI);

         if (!SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE.equals(nsURI))
         {
            return false;
         }
      }
      catch (SOAPException se)
      {
         throw new WebServiceException(se);
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
