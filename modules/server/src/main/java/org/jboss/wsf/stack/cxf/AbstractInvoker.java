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
package org.jboss.wsf.stack.cxf;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;

import org.apache.cxf.attachment.AttachmentImpl;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.frontend.MethodDispatcher;
import org.apache.cxf.headers.Header;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.jaxws.context.WebServiceContextImpl;
import org.apache.cxf.jaxws.context.WrappedMessageContext;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageContentsList;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.invoker.Invoker;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.invocation.EndpointAssociation;
import org.jboss.wsf.spi.invocation.Invocation;
import org.jboss.wsf.spi.invocation.InvocationContext;
import org.jboss.wsf.spi.invocation.InvocationHandler;

/**
 * An abstract CXF invoker
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 06-Dec-2007
 */
public abstract class AbstractInvoker implements Invoker
{
   private static final Object[] NO_ARGS = new Object[]{};
   
   public Object invoke(Exchange exchange, Object o)
   {
      // set up the webservice request context 
      WrappedMessageContext ctx = new WrappedMessageContext(exchange.getInMessage(), Scope.APPLICATION);

      Map<String, Object> handlerScopedStuff = removeHandlerProperties(ctx);

      WebServiceContextImpl.setMessageContext(ctx);

      Object retObj = _invokeInternal(exchange, o, ctx);

      addHandlerProperties(ctx, handlerScopedStuff);

      //update the webservice response context
      updateWebServiceContext(exchange, ctx);
      //clear the WebServiceContextImpl's ThreadLocal variable
      WebServiceContextImpl.clear();

      return new MessageContentsList(retObj);
   }

   private Object _invokeInternal(Exchange exchange, Object o, WrappedMessageContext ctx)
   {
      BindingOperationInfo bop = exchange.get(BindingOperationInfo.class);
      MethodDispatcher md = (MethodDispatcher)exchange.get(Service.class).get(MethodDispatcher.class.getName());
      Method m = md.getMethod(bop);

      Object[] params = NO_ARGS;
      if (m.getParameterTypes().length != 0)
      {
         if (o instanceof List<?>)
         {
            List<Object> paramList = CastUtils.cast((List<?>)o);
            params = paramList.toArray();
         }
         else
         {
            params = new Object[]{o};
         }
      }

      Endpoint ep = EndpointAssociation.getEndpoint();
      InvocationHandler invHandler = ep.getInvocationHandler();

      Invocation inv = invHandler.createInvocation();
      InvocationContext invContext = inv.getInvocationContext();
      inv.getInvocationContext().addAttachment(WebServiceContext.class, getWebServiceContext(ctx));
      invContext.addAttachment(MessageContext.class, ctx);
      inv.setJavaMethod(m);
      inv.setArgs(params);

      Object retObj = null;
      try
      {
         invHandler.invoke(ep, inv);
         retObj = inv.getReturnValue();
      }
      catch (Exception ex)
      {
         handleException(ex);
      }

      return retObj;
   }

   protected abstract WebServiceContext getWebServiceContext(MessageContext msgCtx);

   protected void handleException(Exception ex)
   {
      Throwable th = ex;
      if (ex instanceof InvocationTargetException)
         th = ((InvocationTargetException)ex).getTargetException();

      throw new RuntimeException(th);
   }

   protected Map<String, Object> removeHandlerProperties(WrappedMessageContext ctx)
   {
      Map<String, Scope> scopes = CastUtils.cast((Map<?, ?>)ctx.get(WrappedMessageContext.SCOPES));
      Map<String, Object> handlerScopedStuff = new HashMap<String, Object>();
      if (scopes != null)
      {
         for (Map.Entry<String, Scope> scope : scopes.entrySet())
         {
            if (scope.getValue() == Scope.HANDLER)
            {
               handlerScopedStuff.put(scope.getKey(), ctx.get(scope.getKey()));
            }
         }
         for (String key : handlerScopedStuff.keySet())
         {
            ctx.remove(key);
         }
      }
      return handlerScopedStuff;
   }

   protected void updateWebServiceContext(Exchange exchange, MessageContext ctx)
   {
      // Guard against wrong type associated with header list.
      // Need to copy header only if the message is going out.
      if (ctx.containsKey(Header.HEADER_LIST) && ctx.get(Header.HEADER_LIST) instanceof List<?>)
      {
         List list = (List) ctx.get(Header.HEADER_LIST);
         if (list != null && !list.isEmpty()) {
            SoapMessage sm = (SoapMessage) createResponseMessage(exchange);
            Iterator iter = list.iterator();
            while (iter.hasNext())
            {
               sm.getHeaders().add((Header) iter.next());
            }
         }
      }
      if (exchange.getOutMessage() != null)
      {
         Message out = exchange.getOutMessage();
         if (out.containsKey(Message.PROTOCOL_HEADERS))
         {
            Map<String, List<String>> heads = CastUtils
            .cast((Map<?, ?>)exchange.getOutMessage().get(Message.PROTOCOL_HEADERS));
            if (heads.containsKey("Content-Type")) {
               List<String> ct = heads.get("Content-Type");
               exchange.getOutMessage().put(Message.CONTENT_TYPE, ct.get(0));
               heads.remove("Content-Type");
            }
         }
         Map<String, DataHandler> dataHandlers 
         = CastUtils.cast((Map<?, ?>)out.get(MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS));
         if (dataHandlers != null && !dataHandlers.isEmpty())
         {
            Collection<Attachment> attachments = out.getAttachments();
            if (attachments == null)
            {
               attachments = new ArrayList<Attachment>();
               out.setAttachments(attachments);
            }
            for (Map.Entry<String, DataHandler> entry : dataHandlers.entrySet())
            {
               Attachment att = new AttachmentImpl(entry.getKey(), entry.getValue());
               attachments.add(att);
            }
         }
         out.remove(MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS);
      }
   }

   private Message createResponseMessage(Exchange exchange)
   {
      if (exchange == null) {
         return null;
      }
      Message m = exchange.getOutMessage();
      if (m == null && !exchange.isOneWay()) {
         throw new UnsupportedOperationException();
         /* TODO: below is the copy/paste from CXF 2.1.3 AbstractJAXWSMethodInvoker.java, should we enable it? 
         Endpoint ep = exchange.get(Endpoint.class);
         m = ep.getBinding().createMessage();
         exchange.setOutMessage(m);
         */
      }
      return m;
   }

   protected void addHandlerProperties(WrappedMessageContext ctx, Map<String, Object> handlerScopedStuff)
   {
      for (Map.Entry<String, Object> key : handlerScopedStuff.entrySet())
      {
         ctx.put(key.getKey(), key.getValue(), Scope.HANDLER);
      }
   }

}
