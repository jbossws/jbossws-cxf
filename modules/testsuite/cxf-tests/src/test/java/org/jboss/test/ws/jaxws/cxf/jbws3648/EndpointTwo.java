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
package org.jboss.test.ws.jaxws.cxf.jbws3648;

import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;

import org.jboss.ws.api.annotation.PolicySets;

@WebService(name = "EndpointTwo", targetNamespace = "http://org.jboss.ws.jaxws.cxf/jbws3648")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@PolicySets({"WS-RM_Policy_spec_example", "WS-SP-EX223_WSS11_Anonymous_X509_Sign_Encrypt", "WS-Addressing"})
public interface EndpointTwo
{
   String echo(String input);
}
