/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.webserviceref;

import java.util.Iterator;

import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.wsf.common.handler.GenericSOAPHandler;

/**
 * A client side handler
 *
 */
public class Handler extends GenericSOAPHandler
{

   protected boolean handleInbound(MessageContext msgContext)
   {
      appendHandlerName(msgContext, "in");
      return true;
   }

   protected boolean handleOutbound(MessageContext msgContext)
   {
      appendHandlerName(msgContext, "out");
      return true;
   }

   private boolean appendHandlerName(MessageContext msgContext, String direction)
   {
      try
      {
         SOAPMessage soapMessage = ((SOAPMessageContext)msgContext).getMessage();
         SOAPElement soapElement = getFirstChildElement(soapMessage.getSOAPBody());
         soapElement = getFirstChildElement(soapElement);

         String oldValue = soapElement.getValue();
         String newValue = oldValue + ":" + direction + ":Handler";
         soapElement.setValue(newValue);

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
            return (SOAPElement)currentNode;
         }
      }

      return null;
   }
}
