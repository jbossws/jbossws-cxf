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
package org.jboss.test.ws.jaxws.binding;

import jakarta.xml.soap.MimeHeaders;
import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.logging.Logger;
import org.jboss.ws.api.handler.GenericSOAPHandler;

/**
 * A client side handler
 *
 * @author Alessio Soldano, alessio.soldano@jboss.com
 * @since 31-Oct-2007
 */
public class ClientHandler2 extends GenericSOAPHandler<SOAPMessageContext>
{
   private static Logger log = Logger.getLogger(ClientHandler2.class);

   @Override
   public boolean handleInbound(SOAPMessageContext msgContext)
   {
      log.info("handleInbound");

      try
      {
         SOAPMessage soapMessage = msgContext.getMessage();
         soapMessage.saveChanges(); // force changes save to make sure headers are copied to the message

         MimeHeaders mimeHeaders = soapMessage.getMimeHeaders();
         String[] ct = mimeHeaders.getHeader("Content-Type");
         if (ct != null)
         {
            for (int i = 0; i < ct.length; i++)
            {
               if (ct[i].startsWith(SOAPConstants.SOAP_1_2_CONTENT_TYPE))
                  return true;
            }
         }
         return false;
      }
      catch (SOAPException ex)
      {
         throw new WebServiceException(ex);
      }
   }

   @Override
   protected boolean handleOutbound(SOAPMessageContext msgContext)
   {
      log.info("handleOutbound");

      try
      {
         SOAPMessage soapMessage = msgContext.getMessage();
         soapMessage.saveChanges(); // force changes save to make sure headers are copied to the message

         MimeHeaders mimeHeaders = soapMessage.getMimeHeaders();
         String[] ct = mimeHeaders.getHeader("Content-Type");
         if (ct != null)
         {
            for (int i = 0; i < ct.length; i++)
            {
               if (ct[i].startsWith(SOAPConstants.SOAP_1_2_CONTENT_TYPE))
                  return true;
            }
         }
         return false;
      }
      catch (SOAPException ex)
      {
         throw new WebServiceException(ex);
      }
   }
}
