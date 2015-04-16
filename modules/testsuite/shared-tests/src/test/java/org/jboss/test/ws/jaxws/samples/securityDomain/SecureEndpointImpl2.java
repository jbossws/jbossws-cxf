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
package org.jboss.test.ws.jaxws.samples.securityDomain;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

import org.apache.cxf.interceptor.InInterceptors;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.logging.Logger;
import org.jboss.ws.api.annotation.AuthMethod;
import org.jboss.ws.api.annotation.TransportGuarantee;
import org.jboss.ws.api.annotation.WebContext;

@Stateless(name = "SecureEndpoint")
@SOAPBinding(style = Style.RPC)
@WebService
(
   name = "SecureEndpoint",
   serviceName = "SecureEndpointService2",
   targetNamespace = "http://org.jboss.ws/securityDomain"
)
@WebContext
(
   contextRoot="/jaxws-securityDomain2", 
   urlPattern="/authz",
   authMethod = AuthMethod.BASIC,
   transportGuarantee = TransportGuarantee.NONE,
   secureWSDLAccess = false
)
@SecurityDomain("JBossWSSecurityDomainTest")
public class SecureEndpointImpl2
{
   // Provide logging
   private static Logger log = Logger.getLogger(SecureEndpointImpl2.class);

   @WebMethod
   @PermitAll
   public String echoForAll(String input)
   {
      log.info(input);
      return input;
   }
   
   @RolesAllowed("friend")
   @WebMethod
   public String echo(String input)
   {
      log.info(input);
      return input;
   }
   
   @RolesAllowed("royal")
   @WebMethod
   public String restrictedEcho(String input)
   {
      log.info(input);
      return input;
   }
   
   @WebMethod
   public String defaultAccess(String input)
   {
      log.info(input);
      return input;
   }
}