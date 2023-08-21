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
package org.jboss.test.ws.jaxws.samples.xop.doclit;

import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.ws.api.handler.GenericSOAPHandler;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A MTOM handler should see the conceptual payload,
 * which means an inlined representation of the binary data.
 * It checks existence of the xop:Include element.
 */
public class MTOMProtocolHandler extends GenericSOAPHandler<SOAPMessageContext>
{

   @Override
   protected boolean handleOutbound(SOAPMessageContext msgContext)
   {
      return verifyXOPPackage(msgContext);
   }

   @Override
   protected boolean handleInbound(SOAPMessageContext msgContext)
   {
      return verifyXOPPackage(msgContext);
   }

   private boolean verifyXOPPackage(MessageContext context)
   {
      try
      {
         SOAPMessageContext msgContext = (SOAPMessageContext)context;
         SOAPMessage soapMsg = msgContext.getMessage();
         SOAPEnvelope soapEnv = soapMsg.getSOAPPart().getEnvelope();
         SOAPBody body = soapEnv.getBody();
         boolean found = scanNodes(body.getChildNodes());

         if(found) throw new IllegalStateException("XOP request not properly inlined");

      }
      catch (SOAPException ex)
      {
         throw new WebServiceException(ex);
      }

      return true;
   }

   private boolean scanNodes(NodeList nodes)
   {
      boolean found = false;
      for(int i = 0; i<nodes.getLength(); i++)
      {
         Node n = nodes.item(i);
         if("Include".equals(n.getLocalName()))
         {
            found = true;
            break;
         }
         else
         {
            found = scanNodes(n.getChildNodes());
         }
      }

      return found;
   }
}
