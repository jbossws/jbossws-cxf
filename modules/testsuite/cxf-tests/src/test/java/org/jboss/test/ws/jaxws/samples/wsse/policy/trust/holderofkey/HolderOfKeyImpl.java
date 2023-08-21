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
package org.jboss.test.ws.jaxws.samples.wsse.policy.trust.holderofkey;

import org.apache.cxf.annotations.EndpointProperties;
import org.apache.cxf.annotations.EndpointProperty;

import jakarta.jws.WebService;

@WebService
   (
      portName = "HolderOfKeyServicePort",
      serviceName = "HolderOfKeyService",
      wsdlLocation = "WEB-INF/wsdl/HolderOfKeyService.wsdl",
      targetNamespace = "http://www.jboss.org/jbossws/ws-extensions/holderofkeywssecuritypolicy",
      endpointInterface = "org.jboss.test.ws.jaxws.samples.wsse.policy.trust.holderofkey.HolderOfKeyIface"
   )
@EndpointProperties(value = {
   @EndpointProperty(key = "ws-security.is-bsp-compliant", value = "false"),
   @EndpointProperty(key = "ws-security.signature.properties", value = "serviceKeystore.properties"),
   @EndpointProperty(key = "ws-security.callback-handler", value = "org.jboss.test.ws.jaxws.samples.wsse.policy.trust.holderofkey.HolderOfKeyCallbackHandler")
})
public class HolderOfKeyImpl implements HolderOfKeyIface
{
   public String sayHello()
   {
      return "Holder-Of-Key WS-Trust Hello World!";
   }
}