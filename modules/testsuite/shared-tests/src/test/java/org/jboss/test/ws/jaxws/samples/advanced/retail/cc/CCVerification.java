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
package org.jboss.test.ws.jaxws.samples.advanced.retail.cc;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.xml.ws.RequestWrapper;
import jakarta.xml.ws.ResponseWrapper;


/**
 * This class was generated by the JAXWS SI.
 * JAX-WS RI 2.1-10/21/2006 12:56 AM(vivek)-EA2
 * Generated source version: 2.0
 *
 */
@WebService(name = "CCVerification", targetNamespace = "http://org.jboss.ws/samples/retail/cc")
public interface CCVerification {


    /**
     *
     * @param creditCardNumber
     * @return
     *     returns boolean
     */
    @WebMethod
    @WebResult(name = "verified", targetNamespace = "")
    @RequestWrapper(localName = "verify", targetNamespace = "http://org.jboss.ws/samples/retail/cc", className = "org.jboss.test.ws.jaxws.samples.advanced.retail.cc.VerificationRequest")
    @ResponseWrapper(localName = "verifyResponse", targetNamespace = "http://org.jboss.ws/samples/retail/cc", className = "org.jboss.test.ws.jaxws.samples.advanced.retail.cc.VerificationResponse")
    public Boolean verify(
        @WebParam(name = "creditCardNumber", targetNamespace = "")
        String creditCardNumber);

    //Response<Boolean> verifyAsync(String creditCardNumber);

}
