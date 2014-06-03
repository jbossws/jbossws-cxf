/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.handlerauth;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

public class SimpleHandler implements SOAPHandler<SOAPMessageContext>
{
   public static AtomicInteger counter = new AtomicInteger(0);
   public static AtomicInteger outboundCounter = new AtomicInteger(0);

   @Override
   public boolean handleMessage(SOAPMessageContext context)
   {
      Boolean isOutbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
      String operation = ((QName) context.get(MessageContext.WSDL_OPERATION)).getLocalPart();
      if (!isOutbound && !operation.startsWith("getHandlerCounter")) {
         counter.incrementAndGet();
      } else if (isOutbound && !operation.startsWith("getHandlerCounter")) {
         outboundCounter.incrementAndGet();
      }
      return true;
   }

   @Override
   public boolean handleFault(SOAPMessageContext context)
   {
      String operation = ((QName) context.get(MessageContext.WSDL_OPERATION)).getLocalPart();
      if (!operation.startsWith("getHandlerCounter")) {
         outboundCounter.incrementAndGet();
      }
      return true;
   }

   @Override
   public void close(MessageContext context)
   {
      //NOOP
   }

   @Override
   public Set<QName> getHeaders()
   {
      return null;
   }

}
