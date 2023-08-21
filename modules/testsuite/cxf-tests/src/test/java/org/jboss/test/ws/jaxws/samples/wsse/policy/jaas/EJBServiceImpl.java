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
package org.jboss.test.ws.jaxws.samples.wsse.policy.jaas;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;

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
