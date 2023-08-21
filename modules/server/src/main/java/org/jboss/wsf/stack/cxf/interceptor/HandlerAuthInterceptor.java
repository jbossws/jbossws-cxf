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
package org.jboss.wsf.stack.cxf.interceptor;

import static org.jboss.wsf.stack.cxf.i18n.Messages.MESSAGES;

import java.lang.reflect.Method;
import java.security.Principal;
import java.util.List;

import jakarta.xml.ws.handler.Handler;
import jakarta.xml.ws.handler.LogicalMessageContext;
import jakarta.xml.ws.handler.MessageContext;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxws.context.WrappedMessageContext;
import org.apache.cxf.jaxws.handler.HandlerChainInvoker;
import org.apache.cxf.jaxws.handler.logical.LogicalHandlerInInterceptor;
import org.apache.cxf.jaxws.handler.soap.SOAPHandlerInterceptor;
import org.apache.cxf.jaxws.support.JaxWsEndpointImpl;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.security.SecurityContext;
import org.apache.cxf.service.invoker.MethodDispatcher;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.security.EJBMethodSecurityAttribute;
import org.jboss.wsf.spi.security.EJBMethodSecurityAttributeProvider;

/**
 * Interceptor which checks the current principal is authorized to
 * call a given handler
 * 
 * @author alessio.soldano@jboss.com
 * @since 23-Sep-2013
 */
public class HandlerAuthInterceptor extends AbstractPhaseInterceptor<Message>
{
   private static final String KEY = HandlerAuthInterceptor.class.getName() + ".SECURITY_EXCEPTION";
   
   public HandlerAuthInterceptor()
   {
      super(Phase.PRE_PROTOCOL_FRONTEND);
      addBefore(SOAPHandlerInterceptor.class.getName());
      addBefore(LogicalHandlerInInterceptor.class.getName());
   }

   @Override
   public void handleMessage(Message message) throws Fault
   {
      final Exchange ex = message.getExchange();
      HandlerChainInvoker invoker = ex.get(HandlerChainInvoker.class);
      if (null == invoker)
      {
         final org.apache.cxf.endpoint.Endpoint endpoint = ex.getEndpoint();
         if (endpoint instanceof JaxWsEndpointImpl) { // JAXWS handlers are not assigned to different endpoint types 
            final JaxWsEndpointImpl ep = (JaxWsEndpointImpl)endpoint;
            @SuppressWarnings("rawtypes")
            final List<Handler> handlerChain = ep.getJaxwsBinding().getHandlerChain();
            if (handlerChain != null && !handlerChain.isEmpty()) { //save
               invoker = new JBossWSHandlerChainInvoker(handlerChain, isOutbound(message, ex));
               ex.put(HandlerChainInvoker.class, invoker);
            }
         }
      }
   }
   
   private boolean isOutbound(Message message, Exchange ex) {
      return message == ex.getOutMessage()
          || message == ex.getOutFaultMessage();
   }
   
   private static class JBossWSHandlerChainInvoker extends HandlerChainInvoker
   {

      public JBossWSHandlerChainInvoker(@SuppressWarnings("rawtypes") List<Handler> hc, boolean isOutbound)
      {
         super(hc, isOutbound);
      }

      @Override
      public boolean invokeLogicalHandlers(boolean requestor, LogicalMessageContext context)
      {
         checkAuthorization(context);
         return super.invokeLogicalHandlers(requestor, context);
      }

      @Override
      public boolean invokeProtocolHandlers(boolean requestor, MessageContext context)
      {
         checkAuthorization(context);
         return super.invokeProtocolHandlers(requestor, context);
      }
      
      @Override
      public boolean invokeLogicalHandlersHandleFault(boolean requestor, LogicalMessageContext context)
      {
         if (context.containsKey(KEY)) {
            return true;
         }
         return super.invokeLogicalHandlersHandleFault(requestor, context);
      }

      @Override
      public boolean invokeProtocolHandlersHandleFault(boolean requestor, MessageContext context)
      {
         if (context.containsKey(KEY)) {
            return true;
         }
         return super.invokeProtocolHandlersHandleFault(requestor, context);
      }

      protected void checkAuthorization(MessageContext ctx)
      {
         if ((Boolean) ctx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY))
         {
            return;
         }
         Message message = ((WrappedMessageContext) ctx).getWrappedMessage();
         Exchange exchange = message.getExchange();
         Endpoint ep = exchange.get(Endpoint.class);
         EJBMethodSecurityAttributeProvider attributeProvider = ep
               .getAttachment(EJBMethodSecurityAttributeProvider.class);
         if (attributeProvider != null) //ejb endpoints only can be associated with this...
         {
            SecurityContext secCtx = message.get(SecurityContext.class);
            BindingOperationInfo bop = exchange.getBindingOperationInfo();
            MethodDispatcher md = (MethodDispatcher) exchange.getService().get(MethodDispatcher.class.getName());
            Method method = md.getMethod(bop);

            EJBMethodSecurityAttribute attributes = attributeProvider.getSecurityAttributes(method);
            if (attributes == null || attributes.isPermitAll()) //no security requirement or method marked @PermitAll
            {
               return;
            }
            if (!attributes.isDenyAll())
            {
               if (attributes.getRolesAllowed() != null)
               {
                  for (String role : attributes.getRolesAllowed())
                  {
                     if (secCtx.isUserInRole(role))
                     {
                        return;
                     }
                  }
               }
            }
            final Principal p = secCtx.getUserPrincipal();
            ctx.put(KEY, true);
            throw MESSAGES.authorizationFailed(p != null ? p.getName() : null);
         }
      }
   }

}
