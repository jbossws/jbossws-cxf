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
 * A SOAP 1.2 server side handler
 *
 * @author mageshbk@jboss.com
 * @since 20-Feb-2009
 */
public class SOAP12ServerHandler extends GenericSOAPHandler<SOAPMessageContext>
{
   private static Logger log = Logger.getLogger(SOAP12ServerHandler.class);

   @Override
   public boolean handleInbound(SOAPMessageContext msgContext)
   {
      log.info("handleInbound");

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
      try
      {
         SOAPEnvelope soapEnvelope = ((SOAPMessageContext)msgContext).getMessage().getSOAPPart().getEnvelope();
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

      return true;
   }

   protected ContentType getContentType(SOAPMessageContext msgContext)
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
