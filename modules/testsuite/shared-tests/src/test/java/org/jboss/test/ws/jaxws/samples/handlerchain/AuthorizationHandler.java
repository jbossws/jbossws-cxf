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
package org.jboss.test.ws.jaxws.samples.handlerchain;

import jakarta.xml.soap.Name;
import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPBodyElement;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPFactory;
import jakarta.xml.soap.SOAPHeader;
import jakarta.xml.soap.SOAPHeaderElement;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.logging.Logger;
import org.jboss.ws.api.handler.GenericSOAPHandler;

/**
 * A server side handler
 *
 * @author Thomas.Diesler@jboss.org
 * @since 08-Oct-2005
 */
public class AuthorizationHandler extends GenericSOAPHandler<SOAPMessageContext>
{
   // Provide logging
   private static Logger log = Logger.getLogger(AuthorizationHandler.class);

   @Override
   protected boolean handleInbound(SOAPMessageContext msgContext)
   {
      log.info("handleInbound");

      try
      {
         SOAPMessage soapMessage = msgContext.getMessage();
         SOAPHeader soapHeader = soapMessage.getSOAPHeader();
         SOAPBody soapBody = soapMessage.getSOAPBody();

         SOAPFactory soapFactory = SOAPFactory.newInstance();
         Name headerName = soapFactory.createName("AuthorizationHandlerInbound", "ns1", "http://somens");
         SOAPHeaderElement she = soapHeader.addHeaderElement(headerName);
         she.setValue("true");

         SOAPBodyElement soapBodyElement = (SOAPBodyElement)soapBody.getChildElements().next();
         SOAPElement soapElement = (SOAPElement)soapBodyElement.getChildElements().next();
         String value = soapElement.getValue();
         soapElement.setValue(value + "|AuthIn");
      }
      catch (SOAPException e)
      {
         throw  new WebServiceException(e);
      }

      return true;
   }

   @Override
   protected boolean handleOutbound(SOAPMessageContext msgContext)
   {
      log.info("handleOutbound");

      try
      {
         SOAPMessage soapMessage = msgContext.getMessage();
         SOAPHeader soapHeader = soapMessage.getSOAPHeader();
         SOAPBody soapBody = soapMessage.getSOAPBody();

         SOAPFactory soapFactory = SOAPFactory.newInstance();
         Name headerName = soapFactory.createName("AuthorizationHandlerOutbound", "ns1", "http://somens");
         SOAPHeaderElement she = soapHeader.addHeaderElement(headerName);
         she.setValue("true");

         SOAPBodyElement soapBodyElement = (SOAPBodyElement)soapBody.getChildElements().next();
         SOAPElement soapElement = (SOAPElement)soapBodyElement.getChildElements().next();
         String value = soapElement.getValue();
         soapElement.setValue(value + "|AuthOut");
      }
      catch (SOAPException e)
      {
         throw  new WebServiceException(e);
      }

      return true;
   }
}
