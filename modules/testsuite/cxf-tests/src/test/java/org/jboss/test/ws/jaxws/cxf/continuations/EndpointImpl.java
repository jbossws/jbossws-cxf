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
package org.jboss.test.ws.jaxws.cxf.continuations;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import jakarta.annotation.Resource;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import jakarta.xml.ws.WebServiceContext;

import org.apache.cxf.continuations.Continuation;
import org.apache.cxf.continuations.ContinuationProvider;

@WebService(name = "EndpointService", targetNamespace = "http://org.jboss.ws/cxf/continuations")
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL, parameterStyle = SOAPBinding.ParameterStyle.BARE)
public class EndpointImpl
{
   private final Executor executor = Executors.newCachedThreadPool();
   private static final int TIMEOUT = 2000;

   @Resource
   private WebServiceContext ctx;

   public String echo(String user)
   {
      final ContinuationProvider cp = (ContinuationProvider) ctx.getMessageContext().get(ContinuationProvider.class.getName());
      final Continuation c = cp.getContinuation();
      synchronized (c)
      {
         if (c.isNew())
         {
            FutureTask<String> task = new FutureTask<String>(new MyCallable(user, c));
            c.setObject(task);
            executor.execute(task);
         }
         else
         {
            @SuppressWarnings("unchecked")
            FutureTask<String> task = (FutureTask<String>) c.getObject();
            if (task.isDone())
            {
               try
               {
                  return task.get();
               }
               catch (Exception e)
               {
                  throw new RuntimeException(e);
               }
            }
         }
         c.suspend(TIMEOUT);
      }
      return null;
   }

   private class MyCallable implements Callable<String>
   {

      private final String user;
      private final Continuation c;

      public MyCallable(String user, Continuation c)
      {
         this.user = user;
         this.c = c;
      }

      @Override
      public String call() throws Exception
      {
         try {
            //long running process...
            System.out.println("user: " + user);
            return user + " (ASYNC)";
         } finally {
            c.resume();
         }
      }
   }
}
