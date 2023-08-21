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
