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
package org.jboss.test.ws.jaxws.cxf.asyncclient;

import java.util.concurrent.Future;

import jakarta.annotation.Resource;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import jakarta.xml.ws.AsyncHandler;
import jakarta.xml.ws.Response;
import jakarta.xml.ws.WebServiceContext;

import org.apache.cxf.continuations.Continuation;
import org.apache.cxf.continuations.ContinuationProvider;
/**
 * @author <a href="mailto:ema@redhat.com">Jim Ma</a>
 *
 */
@WebService(name = "EndpointService", targetNamespace = "http://org.jboss.ws/cxf/asyncclient")
@SOAPBinding(style = SOAPBinding.Style.RPC, use = SOAPBinding.Use.LITERAL)
public class EndpointImpl
{
   @Resource
   private WebServiceContext context;
   
   public String echo(long time) {
      ContinuationProvider p = (ContinuationProvider)
            context.getMessageContext().get(ContinuationProvider.class.getName());
        Continuation c = p.getContinuation();
        if (c.isNew()) {
            if (time < 0) {
                c.suspend(-time);
            } else {
                c.suspend(2000 - (time % 1000));
            }
            return null;
        }
        return "Echo:" + time;
   }
   
   public Response<String> echoAsync(long time)
   {
      return null;
   }
   
   public Future<String> echoAsync(final long time, final AsyncHandler<String> handler) {
      return null;
   }
   
}
