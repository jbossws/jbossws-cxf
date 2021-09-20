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
