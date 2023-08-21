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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;

/**
 * A SOAPHandler that checks for Include elements in the
 * outbound SOAPMessage in order to see if MTOM is enabled.
 * 
 * @author alessio.soldano@jboss.com
 * @since 15-Jan-2009
 */
public class MTOMCheckClientHandler implements SOAPHandler<SOAPMessageContext>
{

   public boolean handleMessage(SOAPMessageContext smc)
   {
      try
      {
         return check(smc);
      }
      catch (Exception e)
      {
         throw new WebServiceException(e);
      }
   }

   public boolean handleFault(SOAPMessageContext smc)
   {
      //NOOP
      return true;
   }

   private static boolean check(SOAPMessageContext smc) throws SOAPException, IOException
   {
      Boolean outboundProperty = (Boolean)smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

      if (outboundProperty.booleanValue())
      {
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
         SOAPMessage message = smc.getMessage();
         message.writeTo(outputStream);
         String messageString = outputStream.toString();
         if (!messageString.contains("Include"))
            throw new IllegalStateException("XOP request inlined");
      }
      return true;
   }

   public void close(MessageContext messageContext)
   {
      //NOOP
   }

   public Set<QName> getHeaders()
   {
      return new HashSet<QName>(); //empty set
   }

}
