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

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPFaultException;

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
