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
package org.jboss.test.ws.jaxws.samples.wsse.policy.oasis;

import jakarta.ejb.Stateless;
import jakarta.jws.WebService;

import org.apache.cxf.annotations.EndpointProperties;
import org.apache.cxf.annotations.EndpointProperty;
import org.jboss.ws.api.annotation.WebContext;

@WebService
(
   portName = "SecurityService223Port",
   serviceName = "SecurityService",
   wsdlLocation = "WEB-INF/wsdl/SecurityService.wsdl",
   targetNamespace = "http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy/oasis-samples",
   endpointInterface = "org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.ServiceIface"
)
@EndpointProperties(value = {
      @EndpointProperty(key = "ws-security.signature.properties", value = "bob.properties"),
      @EndpointProperty(key = "ws-security.encryption.properties", value = "bob.properties"),
      @EndpointProperty(key = "ws-security.signature.username", value = "bob"),
      @EndpointProperty(key = "ws-security.callback-handler", value = "org.jboss.test.ws.jaxws.samples.wsse.policy.oasis.KeystorePasswordCallback")
      }
)
@Stateless
@WebContext(urlPattern = "SecurityService223")
public class Service223Impl implements ServiceIface
{
   public String sayHello()
   {
      return "Hello - (WSS1.1) Anonymous with X.509 Certificates, Sign, Encrypt";
   }
}
