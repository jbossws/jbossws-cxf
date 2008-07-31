/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;

import org.apache.cxf.frontend.MethodDispatcher;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.jaxws.context.WebServiceContextImpl;
import org.apache.cxf.jaxws.context.WrappedMessageContext;
import org.apache.cxf.jaxws.support.ContextPropertiesMapping;
import org.apache.cxf.message.Exchange;
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
   public Object invoke(Exchange exchange, Object o)
   {
      // set up the webservice request context 
      MessageContext msgCtx = ContextPropertiesMapping.createWebServiceContext(exchange);

      Map<String, Scope> scopes = CastUtils.cast((Map<?, ?>)msgCtx.get(WrappedMessageContext.SCOPES));
      Map<String, Object> handlerScopedStuff = new HashMap<String, Object>();
      if (scopes != null)
      {
         for (Map.Entry<String, Scope> scope : scopes.entrySet())
         {
            if (scope.getValue() == Scope.HANDLER)
            {
               handlerScopedStuff.put(scope.getKey(), msgCtx.get(scope.getKey()));
            }
         }
         for (String key : handlerScopedStuff.keySet())
         {
            msgCtx.remove(key);
         }
      }

      WebServiceContextImpl.setMessageContext(msgCtx);

      BindingOperationInfo bop = exchange.get(BindingOperationInfo.class);
      MethodDispatcher md = (MethodDispatcher)exchange.get(Service.class).get(MethodDispatcher.class.getName());
      Method m = md.getMethod(bop);

      Object[] params;
      if (o instanceof List<?>)
      {
         List<Object> paramList = CastUtils.cast((List<?>)o);
         params = paramList.toArray();
      }
      else
      {
         params = new Object[]{o};
      }

      Endpoint ep = EndpointAssociation.getEndpoint();
      InvocationHandler invHandler = ep.getInvocationHandler();

      Invocation inv = invHandler.createInvocation();
      InvocationContext invContext = inv.getInvocationContext();
      inv.getInvocationContext().addAttachment(WebServiceContext.class, getWebServiceContext(msgCtx));
      invContext.addAttachment(MessageContext.class, msgCtx);
      inv.setJavaMethod(m);
      inv.setArgs(params);

      Object retObj = null;
      try
      {
         invContext.setTargetBean(getTargetBean(ep));
         invHandler.invoke(ep, inv);
         retObj = inv.getReturnValue();
      }
      catch (Exception ex)
      {
         handleException(ex);
      }

      for (Map.Entry<String, Object> key : handlerScopedStuff.entrySet())
      {
         msgCtx.put(key.getKey(), key.getValue());
         msgCtx.setScope(key.getKey(), Scope.HANDLER);
      }

      //update the webservice response context
      ContextPropertiesMapping.updateWebServiceContext(exchange, msgCtx);
      //clear the WebServiceContextImpl's ThreadLocal variable
      WebServiceContextImpl.clear();

      return new MessageContentsList(retObj);
   }

   protected abstract WebServiceContext getWebServiceContext(MessageContext msgCtx);

   protected Object getTargetBean(Endpoint ep) throws InstantiationException, IllegalAccessException
   {
      Class beanClass = ep.getTargetBeanClass();
      return beanClass.newInstance();
   }

   protected void handleException(Exception ex)
   {
      Throwable th = ex;
      if (ex instanceof InvocationTargetException)
         th = ((InvocationTargetException)ex).getTargetException();

      throw new RuntimeException(th);
   }

}