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

import jakarta.jws.WebService;

import org.apache.cxf.interceptor.InInterceptors;
import org.jboss.ws.api.annotation.EndpointConfig;

@WebService
(
   portName = "SecurityServicePort",
   serviceName = "SecurityService",
   wsdlLocation = "WEB-INF/wsdl/SecurityService.wsdl",
   targetNamespace = "http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy",
   endpointInterface = "org.jboss.test.ws.jaxws.samples.wsse.policy.jaas.ServiceIface"
)
@EndpointConfig(configFile = "WEB-INF/jaxws-endpoint-config.xml", configName = "Custom WS-Security Endpoint")
//be sure to have dependency on org.apache.cxf module when on AS7, otherwise Apache CXF annotations are ignored 
@InInterceptors(interceptors = {
      "org.jboss.wsf.stack.cxf.security.authentication.SubjectCreatingPolicyInterceptor",
      "org.jboss.test.ws.jaxws.samples.wsse.policy.jaas.POJOEndpointAuthorizationInterceptor"}
)
public class ServiceImpl implements ServiceIface
{
   public String sayHello()
   {
      return "Secure Hello World!";
   }
   
   public String greetMe()
   {
      return "Greetings!";
   }
}
