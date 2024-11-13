/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf.interceptor;

import static org.jboss.wsf.stack.cxf.i18n.Messages.MESSAGES;

import java.lang.reflect.Method;
import java.security.Principal;
import java.util.List;

import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;

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
import org.jboss.ws.common.utils.DelegateClassLoader;
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

   private final boolean skip;
   public HandlerAuthInterceptor()
   {
      super(Phase.PRE_PROTOCOL_FRONTEND);
      addBefore(SOAPHandlerInterceptor.class.getName());
      addBefore(LogicalHandlerInInterceptor.class.getName());
      skip = false;
   }
   /**
    * Create a {@code HandlerAuthInterceptor} that can optionally skip authentication.
    * When the authentication is skipped, it added a customized {@code JBossWSHandlerChainInvoker}
    * which set the correct TCCL to allow the handler to access CDI
    * Please see
    * This interceptor will be added to CXF interceptor chain
    * @param skipAuth a boolean flag indicating whether to skip authentication.
    **/
   public HandlerAuthInterceptor(boolean skipAuth)
   {
      super(Phase.PRE_PROTOCOL_FRONTEND);
      addBefore(SOAPHandlerInterceptor.class.getName());
      addBefore(LogicalHandlerInInterceptor.class.getName());
      skip = skipAuth;
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
               invoker = new JBossWSHandlerChainInvoker(handlerChain, isOutbound(message, ex), skip);
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

      private final boolean skip;
      public JBossWSHandlerChainInvoker(@SuppressWarnings("rawtypes") List<Handler> hc, boolean isOutbound)
      {
         super(hc, isOutbound);
         skip = false;
      }

      public JBossWSHandlerChainInvoker(@SuppressWarnings("rawtypes") List<Handler> hc, boolean isOutbound, boolean skipAuth)
      {
         super(hc, isOutbound);
         skip = skipAuth;
      }

      @Override
      public boolean invokeLogicalHandlers(boolean requestor, LogicalMessageContext context)
      {
         if (!skip) {
            checkAuthorization(context);
         }
         ClassLoader original = SecurityActions.getContextClassLoader();
         try {
            if (original instanceof DelegateClassLoader) {
               DelegateClassLoader delegateClassLoader = (DelegateClassLoader)original;
               SecurityActions.setContextClassLoader(delegateClassLoader.getDelegate());
            }
           return super.invokeLogicalHandlers(requestor, context);
         } finally {
            SecurityActions.setContextClassLoader(original);
         }
      }

      @Override
      public boolean invokeProtocolHandlers(boolean requestor, MessageContext context)
      {
         if (!skip) {
            checkAuthorization(context);
         }
         ClassLoader original = SecurityActions.getContextClassLoader();
         try {
            if (original instanceof DelegateClassLoader) {
               DelegateClassLoader delegateClassLoader = (DelegateClassLoader)original;
               SecurityActions.setContextClassLoader(delegateClassLoader.getDelegate());
            }
            return super.invokeProtocolHandlers(requestor, context);
         } finally {
            SecurityActions.setContextClassLoader(original);
         }
      }

      @Override
      public boolean invokeLogicalHandlersHandleFault(boolean requestor, LogicalMessageContext context)
      {

         if (!skip && context.containsKey(KEY)) {
            return true;
         }
         ClassLoader original = SecurityActions.getContextClassLoader();
         try {
            if (original instanceof DelegateClassLoader) {
               DelegateClassLoader delegateClassLoader = (DelegateClassLoader)original;
               SecurityActions.setContextClassLoader(delegateClassLoader.getDelegate());
            }
            return super.invokeLogicalHandlersHandleFault(requestor, context);
         } finally {
            SecurityActions.setContextClassLoader(original);
         }
      }

      @Override
      public boolean invokeProtocolHandlersHandleFault(boolean requestor, MessageContext context)
      {
         if (!skip && context.containsKey(KEY)) {
            return true;
         }
         ClassLoader original = SecurityActions.getContextClassLoader();
         try {
            if (original instanceof DelegateClassLoader) {
               DelegateClassLoader delegateClassLoader = (DelegateClassLoader)original;
               SecurityActions.setContextClassLoader(delegateClassLoader.getDelegate());
            }
            return super.invokeProtocolHandlersHandleFault(requestor, context);
         } finally {
            SecurityActions.setContextClassLoader(original);
         }
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
            if (bop == null)
            {
               throw MESSAGES.missingBindingOperationForAuthorization();
            }
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