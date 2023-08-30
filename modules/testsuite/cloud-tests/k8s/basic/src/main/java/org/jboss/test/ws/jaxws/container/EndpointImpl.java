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
package org.jboss.test.ws.jaxws.container;

import io.dekorate.kubernetes.annotation.KubernetesApplication;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;

import static io.dekorate.kubernetes.annotation.ImagePullPolicy.Always;

/**
 * @author <a href="mailto:ema@redhat.com">Jim Ma</a>
 *
 */
@KubernetesApplication(
        imagePullPolicy = Always,
        replicas = 1)
@WebService(name = "EndpointService", targetNamespace = "http://org.jboss.ws/cxf/container")
@SOAPBinding(style = SOAPBinding.Style.RPC, use = SOAPBinding.Use.LITERAL)
public class EndpointImpl implements Endpoint
{
   public String echo(String input) {
        return "Echo:" + input;
   }
}
