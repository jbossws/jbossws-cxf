/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.wsse.policy.jaas;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebService;

import org.apache.cxf.interceptor.InInterceptors;
import org.jboss.logging.Logger;
import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.ws.api.annotation.EndpointConfig;


@Stateless(name = "EjbEndpoint")
@WebService
(
   portName = "SecurityServicePort",
   serviceName = "SecurityService",
   wsdlLocation = "META-INF/wsdl/SecurityService.wsdl",
   targetNamespace = "http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy",
   endpointInterface = "org.jboss.test.ws.jaxws.samples.wsse.policy.jaas.ServiceIface"
)
@SecurityDomain("JBossWS")
@EndpointConfig(configFile = "META-INF/jaxws-endpoint-config.xml", configName = "Custom WS-Security Endpoint")
public class EJBServiceImpl
{
   // Provide logging
   private static Logger log = Logger.getLogger(EJBServiceImpl.class);

   @WebMethod
   @RolesAllowed("friend")
   public String sayHello()
   {
      log.info("Saying hello");
      return "Secure Hello World!";
   }

   @WebMethod
   @RolesAllowed("snoopies")
   public String greetMe()
   {
      log.info("Greeting");
      return "Greetings!";
   }
}
