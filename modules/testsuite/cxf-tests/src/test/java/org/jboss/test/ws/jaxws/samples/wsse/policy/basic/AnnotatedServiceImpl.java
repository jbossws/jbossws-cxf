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
import org.jboss.ws.api.annotation.PolicySets;
import org.jboss.wsf.stack.cxf.extensions.policy.Constants;

@WebService(
   portName = "AnnotatedSecurityServicePort",
   serviceName = "AnnotatedSecurityService",
   name = "AnnotatedServiceIface",
   endpointInterface = "org.jboss.test.ws.jaxws.samples.wsse.policy.basic.AnnotatedServiceIface",
   targetNamespace = "http://www.jboss.org/jbossws/ws-extensions/wssecuritypolicy"
)
@EndpointProperties(value = {
      @EndpointProperty(key = "ws-security.signature.properties", value = "bob.properties"),
      @EndpointProperty(key = "ws-security.encryption.properties", value = "bob.properties"),
      @EndpointProperty(key = "ws-security.signature.username", value = "bob"),
      @EndpointProperty(key = "ws-security.encryption.username", value = "alice"),
      @EndpointProperty(key = "ws-security.callback-handler", value = "org.jboss.test.ws.jaxws.samples.wsse.policy.basic.KeystorePasswordCallback")
      }
)
@PolicySets(Constants.AsymmetricBinding_X509v1_GCM256OAEP_ProtectTokens_POLICY_SET)
public class AnnotatedServiceImpl implements AnnotatedServiceIface
{
   public String sayHello()
   {
      return "Secure Hello World!";
   }
}
