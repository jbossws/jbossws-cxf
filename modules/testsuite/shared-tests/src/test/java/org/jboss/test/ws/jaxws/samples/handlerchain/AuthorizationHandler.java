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
