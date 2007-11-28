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

//$Id$

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.cxf.frontend.MethodDispatcher;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.invoker.Invoker;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.invocation.EndpointAssociation;
import org.jboss.wsf.spi.invocation.Invocation;
import org.jboss.wsf.spi.invocation.InvocationContext;
import org.jboss.wsf.spi.invocation.InvocationHandler;

/**
 * An CXF invoker for JSE
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 21-May-2007
 */
public class InvokerJSE implements Invoker
{
   public Object invoke(Exchange exchange, Object o)
   {
      BindingOperationInfo bop = exchange.get(BindingOperationInfo.class);
      MethodDispatcher md = (MethodDispatcher)exchange.get(Service.class).get(MethodDispatcher.class.getName());
      Method m = md.getMethod(bop);

      List<Object> paramList = CastUtils.cast((List<?>)o);
      Object[] params = paramList.toArray();

      Endpoint ep = EndpointAssociation.getEndpoint();
      InvocationHandler invHandler = ep.getInvocationHandler();

      Invocation inv = invHandler.createInvocation();
      InvocationContext invContext = inv.getInvocationContext();
      //inv.getInvocationContext().addAttachment(WebServiceContext.class, new WebServiceContextJSE(context));
      //invContext.addAttachment(MessageContext.class, context);
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

      return retObj;
   }

   private Object getTargetBean(Endpoint ep) throws InstantiationException, IllegalAccessException
   {
      Class beanClass = ep.getTargetBeanClass();
      return beanClass.newInstance();
   }

   private void handleException(Exception ex) 
   {
      Throwable th = ex;
      if (ex instanceof InvocationTargetException)
         th = ((InvocationTargetException)ex).getTargetException();

      throw new RuntimeException(th);
   }

}