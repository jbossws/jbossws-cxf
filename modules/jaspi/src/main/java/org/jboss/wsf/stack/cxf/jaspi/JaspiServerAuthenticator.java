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
package org.jboss.wsf.stack.cxf.jaspi;

import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.config.ServerAuthContext;
import javax.xml.namespace.QName;
import jakarta.xml.soap.SOAPMessage;

import org.apache.cxf.binding.soap.Soap12;
import org.apache.cxf.binding.soap.SoapBinding;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.SoapPreProtocolOutInterceptor;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.interceptor.InterceptorChain;
import org.apache.cxf.interceptor.OutgoingChainInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.jboss.security.auth.message.GenericMessageInfo;

/**
 * Authenticator for server side , it is used to authenticate cxf SoapMessage with japsi ServerAuthContext
 * @author <a href="mailto:ema@redhat.com">Jim Ma</a>
 */
public class JaspiServerAuthenticator
{
   public static final String JASPI_SECURITY_DOMAIN = "jaspi.security.domain";

   private final ServerAuthContext sctx;

   public JaspiServerAuthenticator(ServerAuthContext sctx)
   {
      this.sctx = sctx;
   }

   public void validateRequest(SoapMessage message)
   {
      SOAPMessage soapMessage = message.getContent(SOAPMessage.class);
      MessageInfo messageInfo = new GenericMessageInfo(soapMessage, null);
      AuthStatus authStatus;
      try
      {
         authStatus = sctx.validateRequest(messageInfo, null, null);
      }
      catch (AuthException e)
      {
         if (isSOAP12(message))
         {
            SoapFault soap12Fault = new SoapFault(e.getMessage(), Soap12.getInstance().getReceiver());
            throw soap12Fault;
         }
         else
         {
            throw new SoapFault(e.getMessage(), new QName("", "jaspi AuthException"));
         }
      }
      Message response = null;
      if (messageInfo.getResponseMessage() != null && !message.getExchange().isOneWay())
      {

         Endpoint e = message.getExchange().getEndpoint();

         response = new MessageImpl();
         response.setExchange(message.getExchange());
         response = e.getBinding().createMessage(response);
         message.getExchange().setOutMessage(response);
         response.setContent(SOAPMessage.class, messageInfo.getResponseMessage());
         if (AuthStatus.SEND_CONTINUE == authStatus)
         {
            response.put(Message.RESPONSE_CODE, Integer.valueOf(303));
         }
         if (AuthStatus.SEND_FAILURE == authStatus)
         {
            response.put(Message.RESPONSE_CODE, Integer.valueOf(500));
         }

         message.getInterceptorChain().abort();
         InterceptorChain chain = OutgoingChainInterceptor.getOutInterceptorChain(message.getExchange());
         response.setInterceptorChain(chain);
         chain.doInterceptStartingAfter(response, SoapPreProtocolOutInterceptor.class.getName());

      }

   }

   public void secureResponse(SoapMessage message)
   {
      SOAPMessage request = message.getExchange().getInMessage().get(SOAPMessage.class);
      SOAPMessage response = message.getContent(SOAPMessage.class);
      MessageInfo messageInfo = new GenericMessageInfo(request, response);
      AuthStatus authStatus = null;
      try
      {
         authStatus = sctx.secureResponse(messageInfo, null);
      }
      catch (AuthException e)
      {
         if (isSOAP12(message))
         {
            SoapFault soap12Fault = new SoapFault(e.getMessage(), Soap12.getInstance().getReceiver());
            throw soap12Fault;
         }
         else
         {
            throw new SoapFault(e.getMessage(), new QName("", "jaspi AuthException"));
         }
      }
      if (messageInfo.getResponseMessage() != null && !message.getExchange().isOneWay())
      {
         if (AuthStatus.SEND_CONTINUE == authStatus)
         {
            message.put(Message.RESPONSE_CODE, Integer.valueOf(303));
         }
         if (AuthStatus.SEND_FAILURE == authStatus)
         {
            message.put(Message.RESPONSE_CODE, Integer.valueOf(500));
         }
      }

   }

   private boolean isSOAP12(Message message)
   {
      if (message.getExchange().getBinding() instanceof SoapBinding)
      {
         SoapBinding binding = (SoapBinding) message.getExchange().getBinding();
         if (binding.getSoapVersion() == Soap12.getInstance())
         {
            return true;
         }
      }
      return false;
   }

}
