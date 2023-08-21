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
package org.jboss.test.ws.jaxws.handlerauth;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.namespace.QName;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;

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
