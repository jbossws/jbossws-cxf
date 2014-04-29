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
package org.jboss.wsf.stack.cxf;

import java.lang.reflect.Method;
import java.util.List;

import javax.xml.ws.WebServiceContext;
import junit.framework.TestCase;
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


/**
 * A test case for CXF Invoker integration
 * 
 * @author alessio.soldano@jboss.com
 * @since 20-Jun-2012
 * 
 */
public class JBossWSInvokerTest extends TestCase
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
