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

import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.ws.api.handler.GenericSOAPHandler;

/**
 * A server side handler
 *
 * @author alessio.soldano@jboss.com
 * @since 11-Nov-2009
 */
public class ServerHandler extends GenericSOAPHandler<SOAPMessageContext>
{
   private static String nsURI = null;

   @Override
   public boolean handleInbound(SOAPMessageContext msgContext)
   {
      try
      {
         SOAPMessage soapMessage = msgContext.getMessage();
         SOAPEnvelope soapEnvelope = soapMessage.getSOAPPart().getEnvelope();
         nsURI = soapEnvelope.getNamespaceURI();

         return true;
      }
      catch (SOAPException ex)
      {
         throw new WebServiceException(ex);
      }
   }

   public static String getNsURI()
   {
      return nsURI;
   }
}
