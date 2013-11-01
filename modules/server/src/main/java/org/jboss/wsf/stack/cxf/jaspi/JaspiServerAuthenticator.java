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

import java.util.Properties;

import javax.security.auth.Subject;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.config.ServerAuthConfig;
import javax.security.auth.message.config.ServerAuthContext;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;

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
import org.jboss.security.auth.login.JASPIAuthenticationInfo;
import org.jboss.security.auth.message.GenericMessageInfo;
/** 
 * @author <a href="ema@redhat.com">Jim Ma</a>
 */
public class JaspiServerAuthenticator
{
   private ServerAuthConfig serverConfig;
   private String securityDomain;
   private JASPIAuthenticationInfo jpi;

   public JaspiServerAuthenticator(ServerAuthConfig serverConfig, String securityDomain, JASPIAuthenticationInfo jpi)
   {

      this.serverConfig = serverConfig;
      this.securityDomain = securityDomain;
      this.jpi = jpi;
   }

   public void validateRequest(SoapMessage message)
   {
      SOAPMessage soapMessage = message.getContent(SOAPMessage.class);
      MessageInfo messageInfo = new GenericMessageInfo(soapMessage, null);
      String authContextID = serverConfig.getAuthContextID(messageInfo);

      Properties serverContextProperties = new Properties();
      serverContextProperties.put("security-domain", securityDomain);
      serverContextProperties.put("jaspi-policy", jpi);
      Subject clientSubject = new Subject();
      AuthStatus authStatus = null;
      try
      {
         ServerAuthContext sctx = serverConfig.getAuthContext(authContextID, clientSubject, serverContextProperties);
         authStatus = sctx.validateRequest(messageInfo, clientSubject, null);
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
            throw new SoapFault(e.getMessage(), new QName("", "japsi AuthException"));
         }
      }
      Message response = null;
      if (messageInfo.getResponseMessage() != null && !message.getExchange().isOneWay())
      {

         Endpoint e = message.getExchange().get(Endpoint.class);

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
      String authContextID = serverConfig.getAuthContextID(messageInfo);

      Properties serverContextProperties = new Properties();
      serverContextProperties.put("security-domain", securityDomain);
      serverContextProperties.put("jaspi-policy", jpi);
      Subject clientSubject = new Subject();
      AuthStatus authStatus = null;
      try
      {
         ServerAuthContext sctx = serverConfig.getAuthContext(authContextID, clientSubject, serverContextProperties);
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
            throw new SoapFault(e.getMessage(), new QName("", "japsi AuthException"));
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
         SoapBinding binding = (SoapBinding)message.getExchange().getBinding();
         if (binding.getSoapVersion() == Soap12.getInstance())
         {
            return true;
         }
      }
      return false;
   }

}
