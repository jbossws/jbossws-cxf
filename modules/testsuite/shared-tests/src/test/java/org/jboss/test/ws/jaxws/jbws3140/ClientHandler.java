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
package org.jboss.test.ws.jaxws.jbws3140;

import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Set;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

@SuppressWarnings("unchecked")
public class ClientHandler implements SOAPHandler
{

   @Override
   public Set getHeaders()
   {
      // FIXME getHeaders
      return new HashSet();
   }

   @Override
   public void close(MessageContext arg0)
   {
      
   }

   @Override
   public boolean handleFault(MessageContext msgContext)
   {
      return true;
   }

   @Override
   public boolean handleMessage(MessageContext msgContext)
   {
      SOAPMessage message = ((SOAPMessageContext)msgContext).getMessage();
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
