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
package org.jboss.test.ws.jaxws.jbws2978;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import jakarta.jws.WebResult;
import jakarta.xml.ws.RequestWrapper;
import jakarta.xml.ws.ResponseWrapper;

@WebService(name = "AddNumbers", targetNamespace = "http://ws.jboss.org")
public interface AddNumbers {
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "addNumbersRequest", targetNamespace = "http://ws.jboss.org", className = "org.jboss.test.ws.jaxws.jbws2978.AddNumbersRequest")
    @ResponseWrapper(localName = "addNumbersResponse", targetNamespace = "http://ws.jboss.org", className = "org.jboss.test.ws.jaxws.jbws2978.AddNumbersResponse")
    public int addNumbersFault1(
        @WebParam(name = "arg0", targetNamespace = "")
        int arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        int arg1);
}
