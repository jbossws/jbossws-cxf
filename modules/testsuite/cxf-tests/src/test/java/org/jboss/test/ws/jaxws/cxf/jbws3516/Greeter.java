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
package org.jboss.test.ws.jaxws.cxf.jbws3516;

import jakarta.jws.Oneway;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.ws.RequestWrapper;
import jakarta.xml.ws.ResponseWrapper;
@WebService(targetNamespace = "http://jboss.org/hello_world", name = "Greeter")
@XmlSeeAlso({ObjectFactory.class})
public interface Greeter {

    @WebResult(name = "responseType", targetNamespace = "http://jboss.org/hello_world/types")
    @RequestWrapper(localName = "sayHi", targetNamespace = "http://jboss.org/hello_world/types", className = "org.jboss.test.ws.jaxws.cxf.jbws3516.SayHi")
    @WebMethod(action = "sayHiAction")
    @ResponseWrapper(localName = "sayHiResponse", targetNamespace = "http://jboss.org/hello_world/types", className = "org.jboss.test.ws.jaxws.cxf.jbws3516.SayHiResponse")
    public java.lang.String sayHi(
        @WebParam(name = "request", targetNamespace = "http://jboss.org/hello_world/types")
        java.lang.String request
    ) throws SayHiFault;

    @Oneway
    @RequestWrapper(localName = "pingMe", targetNamespace = "http://jboss.org/hello_world/types", className = "org.jboss.hello_world.types.PingMe")
    @WebMethod
    public void pingMe();
}
