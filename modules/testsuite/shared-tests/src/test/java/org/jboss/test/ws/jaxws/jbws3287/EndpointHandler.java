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
package org.jboss.test.ws.jaxws.jbws3287;

import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPBodyElement;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.logging.Logger;
import org.jboss.ws.api.handler.GenericSOAPHandler;

public class EndpointHandler extends GenericSOAPHandler<SOAPMessageContext>
{
   // Provide logging
   private static Logger log = Logger.getLogger(EndpointHandler.class);

   @Override
   protected boolean handleInbound(SOAPMessageContext msgContext)
   {
      log.info("handleInbound");

      try
      {
         SOAPMessage soapMessage = ((SOAPMessageContext)msgContext).getMessage();
         SOAPBody soapBody = soapMessage.getSOAPBody();

         SOAPBodyElement soapBodyElement = (SOAPBodyElement)soapBody.getChildElements().next();
         SOAPElement soapElement = (SOAPElement)soapBodyElement.getChildElements().next();
         String value = soapElement.getValue();
         soapElement.setValue(value + "|EpIn");
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
         SOAPMessage soapMessage = ((SOAPMessageContext)msgContext).getMessage();
         SOAPBody soapBody = soapMessage.getSOAPBody();

         SOAPBodyElement soapBodyElement = (SOAPBodyElement)soapBody.getChildElements().next();
         SOAPElement soapElement = (SOAPElement)soapBodyElement.getChildElements().next();
         String value = soapElement.getValue();
         soapElement.setValue(value + "|EpOut");
      }
      catch (SOAPException e)
      {
         throw  new WebServiceException(e);
      }

      return true;
   }
}
