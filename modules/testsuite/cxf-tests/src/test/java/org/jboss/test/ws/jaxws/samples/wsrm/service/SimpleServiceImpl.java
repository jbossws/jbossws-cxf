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
package org.jboss.test.ws.jaxws.samples.wsrm.service;

import jakarta.jws.Oneway;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;

import org.apache.cxf.interceptor.InInterceptors;

@WebService
(
   name = "SimpleService",
   serviceName = "SimpleService",
   wsdlLocation = "WEB-INF/wsdl/SimpleService.wsdl",
   targetNamespace = "http://www.jboss.org/jbossws/ws-extensions/wsrm"
)
@InInterceptors(interceptors="org.jboss.test.ws.jaxws.samples.wsrm.service.RMCheckInterceptor")
public class SimpleServiceImpl
{
   @Oneway
   @WebMethod
   public void ping()
   {
      System.out.println("ping()");
   }

   @WebMethod
   public String echo(String s)
   {
      System.out.println("echo(" + s + ")");
      return s;
   }
}
