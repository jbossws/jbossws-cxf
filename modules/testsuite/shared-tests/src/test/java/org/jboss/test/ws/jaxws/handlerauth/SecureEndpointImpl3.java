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
package org.jboss.test.ws.jaxws.handlerauth;

import jakarta.annotation.Resource;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.jws.HandlerChain;
import jakarta.jws.Oneway;
import jakarta.jws.WebService;
import jakarta.xml.ws.WebServiceContext;

import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.logging.Logger;
import org.jboss.ws.api.annotation.WebContext;

@WebService(name = "SecureEndpoint3", targetNamespace = "http://ws/")
@HandlerChain(file = "handlers.xml")
@WebContext(contextRoot = "/handlerauth3", urlPattern = "/*", authMethod = "BASIC", transportGuarantee = "NONE", secureWSDLAccess = false)
@Stateless
@SecurityDomain("handlerauth-security-domain")
@RolesAllowed({"user", "friend"})
@DeclareRoles({"user", "friend"})
public class SecureEndpointImpl3 implements SecureEndpoint
{
   private Logger log = Logger.getLogger(this.getClass());

   @Resource
   WebServiceContext context;

   @RolesAllowed("user")
   public String sayHello(String name)
   {
      String principalName = context.getUserPrincipal().getName();
      if (principalName.equals(name)) {
         log.info("sayHello() invoked : Hello, Mr. " + name);
         return "Hello, Mr. " + name;
      } else {
         return "Mr. " + name + ", you authenticated as \'" + principalName + "\'";
      }
   }

   @RolesAllowed("friend")
   public String sayBye(String name)
   {
      String principalName = context.getUserPrincipal().getName();
      if (principalName.equals(name)) {
         log.info("sayBye() invoked : Bye, Mr. " + name);
         return "Bye, Mr. " + name;
      } else {
         return "Mr. " + name + ", you authenticated as \'" + principalName + "\'";
      }
   }

   public int getHandlerCounter() {
      return SimpleHandler.counter.get();
   }

   public int getHandlerCounterOutbound() {
      return SimpleHandler.outboundCounter.get();
   }

   @Oneway
   @RolesAllowed("friend")
   public void ping() {
      //NOOP
   }

   @DenyAll
   public void deniedMethod() {
      //NOOP
   }

   @PermitAll
   public String echo(String s) {
      return s;
   }
}
