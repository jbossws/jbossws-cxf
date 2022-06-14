/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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

@WebService(name = "SecureEndpoint2", targetNamespace = "http://ws/")
@HandlerChain(file = "handlers2.xml")
@WebContext(contextRoot = "/handlerauth2", urlPattern = "/*", authMethod = "BASIC", transportGuarantee = "NONE", secureWSDLAccess = false)
@Stateless
@SecurityDomain("handlerauth-security-domain")
@RolesAllowed({"user", "friend"})
@DeclareRoles({"user", "friend"})
public class SecureEndpointImpl2 implements SecureEndpoint
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
      return LogicalSimpleHandler.counter.get();
   }

   public int getHandlerCounterOutbound() {
      return LogicalSimpleHandler.outboundCounter.get();
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
