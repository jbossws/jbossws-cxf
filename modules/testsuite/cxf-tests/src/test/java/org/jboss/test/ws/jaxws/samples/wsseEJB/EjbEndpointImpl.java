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
package org.jboss.test.ws.jaxws.samples.wsseEJB;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebService;

import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.logging.Logger;


@Stateless(name = "EjbEndpoint")
@WebService
(
   name = "EjbEndpoint",
   serviceName = "EjbEndpointService",
   targetNamespace = "http://org.jboss.ws/wsseEJB"
)
@SecurityDomain("JBossWS")
public class EjbEndpointImpl
{
   // Provide logging
   private static Logger log = Logger.getLogger(EjbEndpointImpl.class);

   @WebMethod
   @RolesAllowed("friend")
   public String sayHello()
   {
      log.info("Saying hello");
      return "hello";
   }

   @WebMethod
   @RolesAllowed("snoopies")
   public String greetMe()
   {
      log.info("Greeting");
      return "greetings";
   }
}
