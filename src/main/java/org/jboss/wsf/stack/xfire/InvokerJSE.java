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
package org.jboss.wsf.stack.xfire;

//$Id$

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.fault.XFireFault;
import org.codehaus.xfire.service.invoker.Invoker;
import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.deployment.EndpointAssociation;
import org.jboss.wsf.spi.invocation.Invocation;
import org.jboss.wsf.spi.invocation.InvocationHandler;

/**
 * An XFire invoker for JSE
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 21-May-2007
 */
public class InvokerJSE implements Invoker
{
   public Object invoke(Method m, Object[] params, MessageContext context) throws XFireFault
   {
      Endpoint ep = EndpointAssociation.getEndpoint();
      InvocationHandler invHandler = ep.getInvocationHandler();

      Invocation inv = invHandler.createInvocation();
      //inv.getInvocationContext().addAttachment(WebServiceContext.class, new WebServiceContextJSE(context));
      inv.getInvocationContext().addAttachment(MessageContext.class, context);
      inv.setJavaMethod(m);
      inv.setArgs(params);

      Object retObj = null;
      try
      {
         Object targetBean = getTargetBean(ep);
         invHandler.invoke(ep, targetBean, inv);
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

   private void handleException(Exception ex) throws XFireFault
   {
      if (ex instanceof InvocationTargetException)
         throw XFireFault.createFault(((InvocationTargetException)ex).getTargetException());

      throw XFireFault.createFault(ex);
   }
}