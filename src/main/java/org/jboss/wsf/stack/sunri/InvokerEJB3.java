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

// $Id$

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.xml.ws.Provider;
import javax.xml.ws.WebServiceException;

import org.jboss.wsf.spi.deployment.Endpoint;
import org.jboss.wsf.spi.invocation.EndpointInvocation;
import org.jboss.wsf.spi.invocation.InvocationHandler;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.server.InstanceResolver;
import com.sun.xml.ws.api.server.Invoker;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.server.WSWebServiceContext;

/**
 * An invoker for EJB3 endpoints
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 10-May-2007
 */
public class InvokerEJB3 extends Invoker
{
   private final InstanceResolver resolver;

   public InvokerEJB3(InstanceResolver resolver)
   {
      this.resolver = resolver;
   }

   @Override
   public void start(@NotNull WSWebServiceContext wsc, @NotNull WSEndpoint endpoint)
   {
      resolver.start(wsc, endpoint);
   }

   @Override
   public void dispose()
   {
      resolver.dispose();
   }

   @Override
   public <T> T invokeProvider(@NotNull Packet p, T arg)
   {
      Object targetBean = resolver.resolve(p);
      return ((Provider<T>)targetBean).invoke(arg);
   }

   @Override
   public Object invoke(Packet p, Method m, Object... args) throws InvocationTargetException, IllegalAccessException
   {
      Endpoint ep = EndpointAssociation.getEndpoint();

      InvocationHandler invHandler = ep.getInvocationHandler();
      EndpointInvocation inv = invHandler.createInvocation();
      inv.setJavaMethod(m);
      inv.setArgs(args);

      Object retObj = null;
      try
      {
         invHandler.invoke(ep, null, inv);
         retObj = inv.getReturnValue();
      }
      catch (Exception ex)
      {
         handleException(ex);
      }

      return retObj;
   }

   private void handleException(Exception ex) throws InvocationTargetException, IllegalAccessException
   {
      if (ex instanceof InvocationTargetException)
         throw (InvocationTargetException)ex;

      if (ex instanceof IllegalAccessException)
         throw (IllegalAccessException)ex;

      throw new WebServiceException(ex);
   }
}