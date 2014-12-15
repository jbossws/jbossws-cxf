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

import java.lang.reflect.Method;
import java.util.List;

import javax.security.auth.callback.CallbackHandler;
import javax.xml.ws.WebServiceContext;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.jaxws.JAXWSMethodInvoker;
import org.apache.cxf.jaxws.context.WebServiceContextImpl;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.MessageContentsList;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.invoker.Factory;
import org.apache.cxf.service.invoker.Invoker;
import org.apache.cxf.service.invoker.MethodDispatcher;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.jboss.security.auth.callback.CallbackHandlerPolicyContextHandler;
import org.jboss.ws.api.util.ServiceLoader;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.invocation.Invocation;
import org.jboss.wsf.spi.invocation.InvocationContext;
import org.jboss.wsf.spi.invocation.InvocationHandler;
import org.jboss.wsf.spi.invocation.NamespaceContextSelectorWrapperFactory;

/**
 * A JBossWS extension of the Apache CXF JAXWSMethodInvoker invoker.
 * 
 * The invocation flow is as follows:
 * 
 *   ServiceInvokerInterceptor::handleMessage(Message m)
 *      |
 *      v
 * JBossWSInvoker::invoke(Exchange e, Object o)
 *      |
 *      v
 *   JAXWSMethodInvoker::invoke(Exchange e, Object o, Method m, Object[] o2)
 *      |
 *      v
 *   AbstractJAXSMethodInvoker::invoke(Exchange e, Object o, Method m, Object[] o2)
 *      |
 *      v
 *   FactoryMethodInvoker::invoke(Exchange e, Object o, Method m, Object[] o2)
 *      |
 *      v
 *   AbstractInvoker::invoke(Exchange e, Object o, Method m, Object[] o2)
 *      |
 *      v
 * JBossWSInvoker::performInvocation(Exchange e, Object o, Method m, Object[] o2)
 * 
 * 
 * @author alessio.soldano@jboss.com
 * @author Thomas.Diesler@jboss.org
 * @author richard.opalka@jboss.com
 * 
 */
public class JBossWSInvoker extends JAXWSMethodInvoker implements Invoker
{
   private volatile Object targetBean;
   private final NamespaceContextSelectorWrapperFactory nsCtxSelectorFactory;

   public JBossWSInvoker() {
      super((Factory)null); //no need for a factory
      ClassLoader cl = ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader();
      nsCtxSelectorFactory = (NamespaceContextSelectorWrapperFactory) ServiceLoader.loadService(
            NamespaceContextSelectorWrapperFactory.class.getName(), null, cl);
   }

   public void setTargetBean(Object targetBean) {
      this.targetBean = targetBean;
   }

   /**
    * This overrides org.apache.cxf.jaxws.AbstractInvoker in order for using the JBoss AS target bean
    * and simplifying the business method matching
    */
   @Override
   public Object invoke(Exchange exchange, Object o)
   {
      BindingOperationInfo bop = exchange.get(BindingOperationInfo.class);
      MethodDispatcher md = (MethodDispatcher) exchange.get(Service.class).get(MethodDispatcher.class.getName());
      List<Object> params = null;
      if (o instanceof List) {
         params = CastUtils.cast((List<?>) o);
      } else if (o != null) {
         params = new MessageContentsList(o);
      }
      if (factory != null)
      {
         targetBean = this.getServiceObject(exchange);
      }

      //[JBWS-3843] workaround: set the CallbackHandler threadlocal again; as a matter of fact, if that's in the Exchange,
      //DIGEST auth is being used and that will cause the EJB layer to re-do authentication because of the bug
      CallbackHandler cbHandler = exchange.getInMessage().get(CallbackHandler.class);
      Object obj = null;
      try
      {
         if (cbHandler != null)
         {
            CallbackHandlerPolicyContextHandler.setCallbackHandler(cbHandler);
         }
         obj = invoke(exchange, targetBean,
               adjustMethodAndParams(md.getMethod(bop), exchange, params, targetBean.getClass()), params);
      }
      finally
      {
         if (cbHandler != null)
         {
            CallbackHandlerPolicyContextHandler.setCallbackHandler(null);
         }
      }
      return obj;
   }

   /**
    * This overrides org.apache.cxf.jaxws.AbstractInvoker in order for using the JBossWS integration logic
    * to invoke the JBoss AS target bean.
    */
   @Override
   protected Object performInvocation(Exchange exchange, final Object serviceObject, Method m, Object[] paramArray)
         throws Exception
   {
      Endpoint ep = exchange.get(Endpoint.class);
      final InvocationHandler invHandler = ep.getInvocationHandler();
      final Invocation inv = createInvocation(invHandler, serviceObject, ep, m, paramArray);
      if (factory != null) {
         inv.getInvocationContext().setProperty("forceTargetBean", true);
      }
      Bus threadBus = BusFactory.getThreadDefaultBus(false);
      BusFactory.setThreadDefaultBus(null);
      setNamespaceContextSelector(exchange);
      
      ClassLoader cl = SecurityActions.getContextClassLoader();
      SecurityActions.setContextClassLoader(serviceObject.getClass().getClassLoader());
      try {
         invHandler.invoke(ep, inv);
         return inv.getReturnValue();
      } finally {
         SecurityActions.setContextClassLoader(cl);
         //make sure the right bus is restored after coming back from the endpoint method
         BusFactory.setThreadDefaultBus(threadBus);
         clearNamespaceContextSelector(exchange);
      }
   }
   
   private Invocation createInvocation(InvocationHandler invHandler, Object serviceObject, Endpoint ep, Method m, Object[] paramArray) {
      Invocation inv = invHandler.createInvocation();
      InvocationContext invContext = inv.getInvocationContext();
      WebServiceContext wsCtx = new WebServiceContextImpl(null);
      invContext.addAttachment(WebServiceContext.class, wsCtx);
      invContext.setTargetBean(serviceObject);
      inv.setJavaMethod(m);
      inv.setArgs(paramArray);
      return inv;
   }

   protected void setNamespaceContextSelector(Exchange exchange)
   {
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
}
