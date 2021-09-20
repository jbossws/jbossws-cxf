/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws2956;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPFault;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;
import jakarta.xml.ws.soap.SOAPFaultException;

import org.jboss.ws.api.handler.GenericSOAPHandler;

public class ClientSOAPHandler extends GenericSOAPHandler<SOAPMessageContext>
{
   @Override
   protected boolean handleInbound(final SOAPMessageContext msgContext)
   {
      //do nothing
      return true;
   }

   protected boolean handleOutbound(final SOAPMessageContext msgContext)
   {
      try
      {
         SOAPFault fault = null;
         MessageFactory factory = MessageFactory.newInstance(); 
         SOAPMessage resMessage = factory.createMessage();
         fault = resMessage.getSOAPBody().addFault();
         fault.setFaultString("this is an exception thrown by client outbound");
         throw new SOAPFaultException(fault);
      }
      catch (SOAPException e)
      {
         //ignore
      }
      return true;
   }
}
