/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
/**
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
import javax.xml.ws.soap.SOAPFaultException;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.attachment.AttachmentImpl;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.frontend.MethodDispatcher;
import org.apache.cxf.headers.Header;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.jaxws.context.WebServiceContextImpl;
import org.apache.cxf.jaxws.context.WrappedMessageContext;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.FaultMode;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageContentsList;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.invoker.Invoker;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.jboss.ws.api.util.ServiceLoader;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.invocation.Invocation;
import org.jboss.wsf.spi.invocation.InvocationContext;
import org.jboss.wsf.spi.invocation.InvocationHandler;
import org.jboss.wsf.spi.invocation.NamespaceContextSelectorWrapperFactory;
import org.jboss.wsf.spi.invocation.WebServiceContextFactory;

/**
 * An abstract CXF invoker
 * 
 * @author Thomas.Diesler@jboss.org
 * @author richard.opalka@jboss.com
 * @author alessio.soldano@jboss.com
 * 
 * @see org.apache.cxf.jaxws.AbstractJAXWSMethodInvoker
 */
public final class JBossWSInvoker implements Invoker
{
   private static final Object[] NO_ARGS = new Object[]{};
   private Object targetBean;
   private WebServiceContextFactory contextFactory;
   private NamespaceContextSelectorWrapperFactory nsCtxSelectorFactory;
   
   public JBossWSInvoker() {
      ClassLoader cl = ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader();
      nsCtxSelectorFactory = (NamespaceContextSelectorWrapperFactory) ServiceLoader.loadService(
            NamespaceContextSelectorWrapperFactory.class.getName(), null, cl);
   }
   
   public void setTargetBean(Object targetBean)
   {
      this.targetBean = targetBean;
   }
   
   public Object invoke(Exchange exchange, Object o)
   {
      // set up the webservice request context 
      WrappedMessageContext ctx = new WrappedMessageContext(exchange.getInMessage(), Scope.APPLICATION);

      Map<String, Object> handlerScopedStuff = removeHandlerProperties(ctx);

      WebServiceContextImpl.setMessageContext(ctx);
      setNamespaceContextSelector(exchange);

      Object retObj = null;
      try
      {
         retObj = _invokeInternal(exchange, o, ctx);

         addHandlerProperties(ctx, handlerScopedStuff);

         //update the webservice response context
         updateWebServiceContext(exchange, ctx);
      }
      finally
      {
         //clear the WebServiceContextImpl's ThreadLocal variable
         WebServiceContextImpl.clear();
         //also cleanup namespace context selector
         clearNamespaceContextSelector(exchange);
      }

      return new MessageContentsList(retObj);
   }

   private Object _invokeInternal(Exchange exchange, Object o, WrappedMessageContext ctx)
   {
      BindingOperationInfo bop = exchange.get(BindingOperationInfo.class);
      MethodDispatcher md = (MethodDispatcher)exchange.get(Service.class).get(MethodDispatcher.class.getName());
      Method m = md.getMethod(bop);

      Object[] params = NO_ARGS;
      List<Object> paramList = null;
      if (m.getParameterTypes().length != 0)
      {
         if (o instanceof List<?>)
         {
            paramList = CastUtils.cast((List<?>)o);
            params = paramList.toArray();
         }
         else
         {
            params = new Object[]{o};
         }
      }

      Endpoint ep = exchange.get(Endpoint.class);
      InvocationHandler invHandler = ep.getInvocationHandler();

      Invocation inv = invHandler.createInvocation();
      InvocationContext invContext = inv.getInvocationContext();
      invContext.addAttachment(WebServiceContext.class, getWebServiceContext(ctx));
      invContext.addAttachment(MessageContext.class, ctx);
      invContext.setTargetBean(targetBean != null ? targetBean : ep.getAttachment(Object.class)); //JBWS-2486 - JBWS-3002
      inv.setJavaMethod(m);
      inv.setArgs(params);

      Object retObj = null;
      Bus threadBus = BusFactory.getThreadDefaultBus(false);
      try
      {
         invHandler.invoke(ep, inv);
         retObj = inv.getReturnValue();
      } catch (InvocationTargetException e) {
         Throwable t = e.getCause();
         if (t == null) {
            t = e;
         }
         exchange.getInMessage().put(FaultMode.class, FaultMode.UNCHECKED_APPLICATION_FAULT);
         for (Class<?> cl : m.getExceptionTypes()) {
            if (cl.isInstance(t)) {
               exchange.getInMessage().put(FaultMode.class, 
                     FaultMode.CHECKED_APPLICATION_FAULT);                    
            }
         }

         if (t instanceof Fault) {
            exchange.getInMessage().put(FaultMode.class, 
                  FaultMode.CHECKED_APPLICATION_FAULT);                    
            throw (Fault)t;
         }
         throw createFault(t, m, paramList, true);
      } catch (Fault f) {
         exchange.getInMessage().put(FaultMode.class, FaultMode.UNCHECKED_APPLICATION_FAULT);
         throw f;
      } catch (Exception e) {
         exchange.getInMessage().put(FaultMode.class, FaultMode.UNCHECKED_APPLICATION_FAULT);
         throw createFault(e, m, paramList, false);
      }
      finally
      {
         //make sure the right bus is restored after coming back from the endpoint method
         BusFactory.setThreadDefaultBus(threadBus);
         // JBWS-2486
         if (ep.getAttachment(Object.class) == null)
         {
            ep.addAttachment(Object.class, inv.getInvocationContext().getTargetBean());
         }
      }

      return retObj;
   }
   
   protected SOAPFaultException findSoapFaultException(Throwable ex) {
      if (ex instanceof SOAPFaultException) {
          return (SOAPFaultException)ex;
      }
      if (ex.getCause() != null) {
          return findSoapFaultException(ex.getCause());
      }
      return null;
  }
  
  protected Fault createFault(Throwable ex, Method m, List<Object> params, boolean checked) {
      //map the JAX-WS faults
      SOAPFaultException sfe = findSoapFaultException(ex);
      if (sfe != null) {
          SoapFault fault = new SoapFault(sfe.getFault().getFaultString(),
                                          sfe,
                                          sfe.getFault().getFaultCodeAsQName());
          fault.setRole(sfe.getFault().getFaultActor());
          fault.setDetail(sfe.getFault().getDetail());
          
          return fault;
      }
      
      return new Fault(ex);
  }


   protected WebServiceContext getWebServiceContext(MessageContext msgCtx)
   {
      return getWebServiceContextFactory().newWebServiceContext(msgCtx);
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
         List<?> list = (List<?>) ctx.get(Header.HEADER_LIST);
         if (list != null && !list.isEmpty()) {
            SoapMessage sm = (SoapMessage) createResponseMessage(exchange);
            if (sm != null)
            {
               Iterator<?> iter = list.iterator();
               while (iter.hasNext())
               {
                  sm.getHeaders().add((Header) iter.next());
               }
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
      if (m == null && !exchange.isOneWay())
      {
         org.apache.cxf.endpoint.Endpoint ep = exchange.get(org.apache.cxf.endpoint.Endpoint.class);
         m = ep.getBinding().createMessage();
         exchange.setOutMessage(m);
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
   
   protected void setNamespaceContextSelector(Exchange exchange) {
      if (exchange.isOneWay() && nsCtxSelectorFactory != null)
      {
         nsCtxSelectorFactory.getWrapper().setCurrentThreadSelector(exchange);
      }
   }
   
   protected void clearNamespaceContextSelector(Exchange exchange)
   {
      if (exchange.isOneWay() && nsCtxSelectorFactory != null)
      {
         nsCtxSelectorFactory.getWrapper().clearCurrentThreadSelector(exchange);
      }
   }
   
   protected synchronized WebServiceContextFactory getWebServiceContextFactory()
   {
      if (contextFactory == null)
      {
         ClassLoader cl = ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader();
         SPIProvider spiProvider = SPIProviderResolver.getInstance(cl).getProvider();
         contextFactory = spiProvider.getSPI(WebServiceContextFactory.class, cl);
      }
      return contextFactory;
   }
   
}
