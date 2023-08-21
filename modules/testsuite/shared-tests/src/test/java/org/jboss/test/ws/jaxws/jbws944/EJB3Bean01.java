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
package org.jboss.test.ws.jaxws.jbws944;

import jakarta.ejb.Remote;
import jakarta.ejb.RemoteHome;
import jakarta.ejb.Stateless;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;

import org.jboss.ws.api.annotation.WebContext;

@WebService(name = "EJB3Bean", serviceName = "EJB3BeanService", targetNamespace = "http://org.jboss.ws/jbws944")
@WebContext(contextRoot = "/jaxws-jbws944", urlPattern = "/FooBean01")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@Remote({EJB3RemoteBusinessInterface.class})
@RemoteHome(EJB3RemoteHome.class)
@Stateless(name = "FooBean01")
public class EJB3Bean01 implements EJB3RemoteBusinessInterface
{
   @WebMethod
   public String echo(String input)
   {
      return input;
   }
}
