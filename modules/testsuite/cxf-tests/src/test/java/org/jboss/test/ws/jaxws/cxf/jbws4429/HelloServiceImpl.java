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
package org.jboss.test.ws.jaxws.cxf.jbws4429;

import jakarta.ejb.Stateless;
import jakarta.jws.HandlerChain;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;

@Stateless
//@WebService(targetNamespace ="http://com.redhat.gss.example.soap/") // correct targetNamespace
@WebService(targetNamespace ="http://com.redhat.gss.invalid/")        // invalid targetNamespace that is not matched with wsdl distributed to clients
@HandlerChain(file = "/handlers.xml")
public class HelloServiceImpl {
    @WebMethod
    public String sayHello(String name) {
        return "Hello, " + name;
    }
}
