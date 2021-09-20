/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws1799;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
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
   @TransactionAttribute(javax.ejb.TransactionAttributeType.SUPPORTS)
   public boolean authenticate(String username)
   {
       return "authorized".equals(username);
   }
}
