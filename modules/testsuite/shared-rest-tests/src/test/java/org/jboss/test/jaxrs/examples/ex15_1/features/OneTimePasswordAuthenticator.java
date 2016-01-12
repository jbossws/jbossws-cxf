/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.jaxrs.examples.ex15_1.features;

import javax.annotation.Priority;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.security.Principal;
import java.util.Map;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 */
@OTPAuthenticated
@Priority(Priorities.AUTHENTICATION)
public class OneTimePasswordAuthenticator implements ContainerRequestFilter
{
   protected Map<String, String> userSecretMap;

   public OneTimePasswordAuthenticator(Map<String, String> userSecretMap)
   {
      this.userSecretMap = userSecretMap;
   }

   @Override
   public void filter(ContainerRequestContext requestContext) throws IOException
   {
      String authorization = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
      if (authorization == null) throw new NotAuthorizedException("OTP");

      String[] split = authorization.split(" ");
      final String user = split[0];
      String otp = split[1];

      String secret = userSecretMap.get(user);
      if (secret == null) throw new NotAuthorizedException("OTP");

      String regen = OTP.generateToken(secret);
      if (!regen.equals(otp)) throw new NotAuthorizedException("OTP");

      final SecurityContext securityContext = requestContext.getSecurityContext();
      requestContext.setSecurityContext(new SecurityContext()
      {
         @Override
         public Principal getUserPrincipal()
         {
            return new Principal()
            {
               @Override
               public String getName()
               {
                  return user;
               }
            };
         }

         @Override
         public boolean isUserInRole(String role)
         {
            return false;
         }

         @Override
         public boolean isSecure()
         {
            return securityContext.isSecure();
         }

         @Override
         public String getAuthenticationScheme()
         {
            return "OTP";
         }
      });
   }
}