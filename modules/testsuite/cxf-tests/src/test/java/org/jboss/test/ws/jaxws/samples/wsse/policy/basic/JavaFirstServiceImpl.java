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
package org.jboss.test.ws.jaxws.samples.wsse.policy.basic;

import jakarta.jws.WebService;

import org.apache.cxf.annotations.EndpointProperties;
import org.apache.cxf.annotations.EndpointProperty;
import org.apache.cxf.annotations.Policy;

@WebService
(
   portName = "JavaFirstSecurityServicePort",
   serviceName = "JavaFirstSecurityService",
   name = "JavaFirstServiceIface",
   targetNamespace = "http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy"
)
@Policy(placement = Policy.Placement.BINDING, uri = "JavaFirstPolicy.xml")
@EndpointProperties(value = {
      @EndpointProperty(key = "ws-security.callback-handler", value = "org.jboss.test.ws.jaxws.samples.wsse.policy.basic.ServerUsernamePasswordCallback")
      }
)
public class JavaFirstServiceImpl //Not extending JavaFirstServiceIface for testing purposes only, to avoid having to
                                  //move the @Policy annotation in the interface, which is also used on client side. 
{
   public String sayHello()
   {
      return "Secure Hello World!";
   }
}
