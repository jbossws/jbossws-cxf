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
package org.jboss.test.ws.jaxws.samples.asynch;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import jakarta.xml.ws.AsyncHandler;

import org.apache.cxf.annotations.UseAsyncMethod;

@WebService(name = "EndpointService", targetNamespace = "http://org.jboss.ws/cxf/samples/asynch")
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL, parameterStyle = SOAPBinding.ParameterStyle.BARE)
public class EndpointImpl
{
   //be sure to add dependency on org.apache.cxf module for @UseAsyncMethod annotation to be resolved
   @UseAsyncMethod //.. if supported by underlying container
   public String echo(String user)
   {
      System.out.println("user: " + user);
      return user + " (SYNC)";
   }
   
   //this method is not actually converted into an operation in the wsdl contract, as it's up to the
   //Apache CXF implementation to redirect invocation to this when the @UseAsyncMethod annotated
   //echo method is called.
   public Future<?> echoAsync(final String user, final AsyncHandler<String> asyncHandler)
   {
      final ServerAsyncResponse<String> r = new ServerAsyncResponse<String>();
      //here the business method would likely enqueue the request for a long running process;
      //then the queue might for instance be consumed by a different thread pool...
      new Thread() {
         public void run() {
            r.set(user + " (ASYNC)");
            System.out.println("Responding on background thread...");
            asyncHandler.handleResponse(r);
         }
      }.start();
      //the web container thread proceeds further and the container is able to serve new web requests with it
      return r;
   }
   
   //helper class
   private class ServerAsyncResponse<T> implements jakarta.xml.ws.Response<T> {
      T value;
      boolean done;
      Throwable throwable;
      
      /**
       * Currently unused
       */
      public boolean cancel(boolean mayInterruptIfRunning) {
          return false;
      }
      /**
       * Currently unused
       */
      public boolean isCancelled() {
          return false;
      }
      public boolean isDone() {
          return done;
      }
      public void set(T t) {
          value = t;
          done = true;
      }
      public T get() throws InterruptedException, ExecutionException {
          if (throwable != null) {
              throw new ExecutionException(throwable);
          }
          return value;
      }
      public T get(long timeout, TimeUnit unit) 
          throws InterruptedException, ExecutionException, TimeoutException {
          return value;
      }
      /**
       * Currently unused
       */
      public Map<String, Object> getContext() {
          return null;
      }
  }
}
