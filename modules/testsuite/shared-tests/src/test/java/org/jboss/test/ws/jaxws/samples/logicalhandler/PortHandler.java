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
package org.jboss.test.ws.jaxws.samples.logicalhandler;

import java.util.Iterator;

import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.logging.Logger;
import org.jboss.ws.api.handler.GenericSOAPHandler;

/**
 * A jaxws protocol handler
 *
 * @author Thomas.Diesler@jboss.org
 * @since 24-Nov-2005
 */
public class PortHandler extends GenericSOAPHandler<SOAPMessageContext>
{
   // provide logging
   private static final Logger log = Logger.getLogger(PortHandler.class);

   @Override
   public boolean handleOutbound(SOAPMessageContext msgContext)
   {
      return appendHandlerName(msgContext, "Outbound");
   }

   @Override
   public boolean handleInbound(SOAPMessageContext msgContext)
   {
      return appendHandlerName(msgContext, "Inbound");
   }

   private boolean appendHandlerName(SOAPMessageContext msgContext, String direction)
   {
      try
      {
         SOAPMessage soapMessage = msgContext.getMessage();
         SOAPElement soapElement = getFirstChildElement(soapMessage.getSOAPBody());
         soapElement = getFirstChildElement(soapElement);

         String oldValue = soapElement.getValue();
         String newValue = oldValue + ":" + direction + ":PortHandler";
         soapElement.setValue(newValue);

         log.debug("oldValue: " + oldValue);
         log.debug("newValue: " + newValue);

         return true;
      }
      catch (SOAPException ex)
      {
         throw new WebServiceException(ex);
      }
   }

   private SOAPElement getFirstChildElement(SOAPElement parentNode)
   {
      Iterator<?> i = parentNode.getChildElements();
      while (i.hasNext())
      {
         Object currentNode = i.next();
         if (currentNode instanceof SOAPElement)
         {
            return (SOAPElement) currentNode;
         }
      }

      return null;
   }
}
