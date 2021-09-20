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
