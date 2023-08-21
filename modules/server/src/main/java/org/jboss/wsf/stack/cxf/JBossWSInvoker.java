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

import jakarta.xml.ws.WebServiceContext;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.jaxws.JAXWSMethodInvoker;
import org.apache.cxf.jaxws.context.WebServiceContextImpl;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.MessageContentsList;
import org.apache.cxf.service.invoker.Factory;
import org.apache.cxf.service.invoker.Invoker;
import org.apache.cxf.service.invoker.MethodDispatcher;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.jboss.ws.api.util.ServiceLoader;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.invocation.Invocation;
import org.jboss.wsf.spi.invocation.InvocationContext;
import org.jboss.wsf.spi.invocation.InvocationHandler;
import org.jboss.wsf.spi.invocation.NamespaceContextSelectorWrapperFactory;
import org.jboss.wsf.stack.cxf.client.Constants;
import org.jboss.wsf.stack.cxf.i18n.Messages;

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
   private static final boolean disableDepUserDefThreadBus = Boolean.getBoolean(Constants.JBWS_CXF_DISABLE_DEPLOYMENT_USER_DEFAULT_THREAD_BUS);
   
   private Object targetBean;
   private final NamespaceContextSelectorWrapperFactory nsCtxSelectorFactory;
   private final boolean checkForUseAsyncMethod;

   public JBossWSInvoker() {
      this(true);
   }
   
   public JBossWSInvoker(boolean checkForUseAsyncMethod) {
      super((Factory)null); //no need for a factory
      ClassLoader cl = ClassLoaderProvider.getDefaultProvider().getServerIntegrationClassLoader();
      nsCtxSelectorFactory = (NamespaceContextSelectorWrapperFactory) ServiceLoader.loadService(
            NamespaceContextSelectorWrapperFactory.class.getName(), null, cl);
      this.checkForUseAsyncMethod = checkForUseAsyncMethod;
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
      BindingOperationInfo bop = exchange.getBindingOperationInfo();
      MethodDispatcher md = (MethodDispatcher) exchange.getService().get(MethodDispatcher.class.getName());
      List<Object> params = null;
      if (o instanceof List) {
         params = CastUtils.cast((List<?>) o);
      } else if (o != null) {
         params = new MessageContentsList(o);
      }
      final Object tb = (factory == null) ? targetBean : this.getServiceObject(exchange);
      final Method method = (bop == null) ? null : md.getMethod(bop);
      if (method == null)
      {
         throw Messages.MESSAGES.missingBindingOpeartionAndDispatchedMethod();
      }
      //performance optimization, adjustMethodAndParams currently looks for @UseAsyncMethod (which is expensive) and only performs actions if it's found
      final Method fm = checkForUseAsyncMethod ? adjustMethodAndParams(md.getMethod(bop), exchange, params, tb.getClass()) : method;
      return invoke(exchange, tb, fm, params);
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
      BusFactory.setThreadDefaultBus(disableDepUserDefThreadBus ? null : ep.getAttachment(Bus.class));
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
