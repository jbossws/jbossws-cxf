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
package org.jboss.wsf.stack.cxf;

import java.lang.reflect.Method;
import java.util.List;

import jakarta.xml.ws.WebServiceContext;
import org.apache.cxf.jaxws.context.WebServiceContextImpl;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.ServiceImpl;
import org.apache.cxf.service.invoker.MethodDispatcher;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * A test case for CXF Invoker integration
 * 
 * @author alessio.soldano@jboss.com
 * @since 20-Jun-2012
 * 
 */
public class JBossWSInvokerTest
{
   /**
    * The JBossWSInvoker internals rely on the Apache CXF side of the Invoker abstraction to properly
    * set the generated WrappedMessageContext into the WebServiceContextImpl's threadlocal. As a consequence,
    * the JBossWSInvoker::performInvocation method can build up the JBoss integration WebServiceContext without
    * needing to pass in a MessageContext, as the properly created one is automatically retrieved internally
    * using the Apache CXF thread local. This all however assumes the Invoker inheritance tree is not erroneously
    * changed or the integration is not inadvertently broken in some way. 
    * This test hence verifies the MessageContext instance is available inside the performInvocation method.
    */
   @Test
   public void testMessageContextThreadLocal()
   {
      TestInvoker invoker = new TestInvoker();
      invoker.setTargetBean(this); //just to avoid internal NPE
      Exchange exchange = getTestExchange();
      Object obj = invoker.invoke(exchange, null);
      String res = obj instanceof List<?> ? ((List<?>)obj).get(0).toString() : obj.toString();
      assertEquals("OK", res);
   }
   
   private static class TestInvoker extends JBossWSInvoker
   {
      @Override
      protected Object performInvocation(Exchange exchange, final Object serviceObject, Method m, Object[] paramArray)
            throws Exception
      {
         WebServiceContext wsCtx = new WebServiceContextImpl(null);
         return wsCtx.getMessageContext() != null ? "OK" : "FAIL";
      }
   }
   
   //build up a fake exchange instance, the minimum required to let the flow proceed till the JBossWSInvoker
   private Exchange getTestExchange() {
      Exchange exchange = new ExchangeImpl();
      Message message = new MessageImpl();
      message.setExchange(exchange);
      exchange.setInMessage(message);
      exchange.put(BindingOperationInfo.class, new BindingOperationInfo());
      Service service = new ServiceImpl();
      MethodDispatcher md = new MethodDispatcher() {
         @Override
         public Method getMethod(BindingOperationInfo op) {
            return this.getClass().getMethods()[0];
         }
         @Override
         public BindingOperationInfo getBindingOperation(Method m, Endpoint endpoint) {
            return null;
         }
         @Override
         public void bind(OperationInfo o, Method... methods) {
         }
      };
      service.put(MethodDispatcher.class.getName(), md);
      exchange.put(Service.class, service);
      return exchange;
   }
}
