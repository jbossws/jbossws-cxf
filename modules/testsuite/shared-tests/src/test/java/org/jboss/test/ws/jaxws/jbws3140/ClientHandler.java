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
package org.jboss.test.ws.jaxws.jbws3140;

import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;

public class ClientHandler implements SOAPHandler<SOAPMessageContext>
{

   @Override
   public Set<QName> getHeaders()
   {
      // FIXME getHeaders
      return new HashSet<QName>();
   }

   @Override
   public void close(MessageContext arg0)
   {

   }

   @Override
   public boolean handleFault(SOAPMessageContext msgContext)
   {
      return true;
   }

   @Override
   public boolean handleMessage(SOAPMessageContext msgContext)
   {
      SOAPMessage message = msgContext.getMessage();
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      try
      {
         message.writeTo(bout);
      }
      catch (Exception e)
      {
       //do nothing
      }
      if (isOutbound(msgContext))
      {
          if (isMTOMEnabled(bout.toString()))
          {
             ServletTestClient.resultTrace.append("--ClientMTOMEnabled");
          }
          else
          {
             ServletTestClient.resultTrace.append("--ClientMTOMNotEnabled");
          }
      }
      else
      {
         if (isMTOMEnabled(bout.toString()))
         {
            ServletTestClient.resultTrace.append("--ServerMTOMEnabled");
         }
         else
         {
            ServletTestClient.resultTrace.append("--ServerMTOMNotEnabled");
         }
         if (isWSAEnabled(bout.toString()))
         {
            ServletTestClient.resultTrace.append("--ServerAddressingEnabled");
         }
         else
         {
            ServletTestClient.resultTrace.append("--ServerAddressingNotEnabled");
         }

      }
      return true;
   }

   protected Boolean isOutbound(MessageContext msgContext)
   {
      Boolean outbound = (Boolean)msgContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
      if (outbound == null)
         throw new IllegalStateException("Cannot find property: " + MessageContext.MESSAGE_OUTBOUND_PROPERTY);

      return outbound;
   }

   public boolean isMTOMEnabled(String str) {

      if (str.indexOf("http://www.w3.org/2004/08/xop/include") > -1) {
         return true;
      }
      return false;

   }

   public boolean isWSAEnabled(String str) {
      if (str.indexOf("Action") > -1)
      {
         return true;
      }
      return false;
   }

}
