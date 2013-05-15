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
package org.jboss.test.ws.jaxrpc.samples.exception;

import javax.xml.namespace.QName;
import javax.xml.rpc.JAXRPCException;
import javax.xml.rpc.handler.GenericHandler;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.handler.soap.SOAPMessageContext;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;

public class ServerHandler extends GenericHandler
{

   public QName[] getHeaders()
   {
      return new QName[] {};
   }

   // http://jira.jboss.org/jira/browse/JBWS-668
   // Handler.handleFault does not give access to the fault message
   public boolean handleFault(MessageContext msgContext)
   {
      try
      {
         SOAPMessage soapMessage = ((SOAPMessageContext)msgContext).getMessage();

         SOAPFault soapFault = soapMessage.getSOAPBody().getFault();
         String faultString = soapFault.getFaultString();
         if (!faultString.equals("Don't worry it's just a test") && !faultString.equals("org.jboss.test.ws.jaxrpc.samples.exception.UserException"))
            throw new JAXRPCException("Unexpected fault string: " + faultString);

         return true;

      }
      catch (SOAPException e)
      {
         throw new JAXRPCException(e.toString(), e);
      }
   }
}
