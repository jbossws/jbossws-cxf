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
package org.jboss.test.ws.jaxws.jbws2278;

import jakarta.xml.soap.MimeHeaders;
import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.soap.SOAPPart;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;

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
