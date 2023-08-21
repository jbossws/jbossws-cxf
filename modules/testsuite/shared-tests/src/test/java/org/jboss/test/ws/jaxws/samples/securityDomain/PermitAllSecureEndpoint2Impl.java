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
package org.jboss.test.ws.jaxws.samples.securityDomain;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import jakarta.jws.soap.SOAPBinding.Style;

import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.logging.Logger;
import org.jboss.ws.api.annotation.AuthMethod;
import org.jboss.ws.api.annotation.TransportGuarantee;
import org.jboss.ws.api.annotation.WebContext;

@Stateless(name = "PermitAllSecureEndpoint2")
@SOAPBinding(style = Style.RPC)
@WebService
(
   name = "PermitAllSecureEndpoint2",
   serviceName = "PermitAllSecureEndpoint2Service",
   targetNamespace = "http://org.jboss.ws/securityDomain"
)
@WebContext
(
   contextRoot="/jaxws-securityDomain-permitall", 
   urlPattern="/two",
   authMethod = AuthMethod.BASIC,
   transportGuarantee = TransportGuarantee.NONE,
   secureWSDLAccess = false
)
@PermitAll
@SecurityDomain("JBossWSSecurityDomainPermitAllTest")
public class PermitAllSecureEndpoint2Impl
{
   // Provide logging
   private static Logger log = Logger.getLogger(PermitAllSecureEndpoint2Impl.class);

   @WebMethod
   @RolesAllowed("friend")
   public String echo(String input)
   {
      log.info(input);
      return input;
   }

   @WebMethod
   public String echoForAll(String input)
   {
      log.info(input);
      return input;
   }
}
