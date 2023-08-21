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
package org.jboss.test.ws.jaxws.jbws1799;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.jws.WebService;

import org.jboss.ws.api.annotation.TransportGuarantee;
import org.jboss.ws.api.annotation.WebContext;

/**
 * First service implementation
 *
 * @author richard.opalka@jboss.com
 *
 * @since Oct 8, 2007
 */
@Stateless
@WebService
(
   targetNamespace = "namespace1",
   serviceName = "UserAccountService1.0",
   endpointInterface = "org.jboss.test.ws.jaxws.jbws1799.IUserAccountService"
)
@WebContext
(
   transportGuarantee = TransportGuarantee.NONE,
   contextRoot = "/svc-useracctv1.0",
   urlPattern = "/UserAccountService1.0"
)
public class UserAccountService implements IUserAccountService
{
   @TransactionAttribute(jakarta.ejb.TransactionAttributeType.SUPPORTS)
   public boolean authenticate(String username)
   {
       return "authorized".equals(username);
   }
}
