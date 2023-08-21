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
package org.jboss.test.ws.jaxws.samples.exception.client;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import jakarta.xml.ws.RequestWrapper;
import jakarta.xml.ws.ResponseWrapper;


@WebService(name = "ExceptionEndpoint", targetNamespace = "http://server.exception.samples.jaxws.ws.test.jboss.org/")
public interface ExceptionEndpoint {


    /**
     * 
     */
    @WebMethod
    @RequestWrapper(localName = "throwRuntimeException", targetNamespace = "http://server.exception.samples.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.samples.exception.client.ThrowRuntimeException")
    @ResponseWrapper(localName = "throwRuntimeExceptionResponse", targetNamespace = "http://server.exception.samples.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.samples.exception.client.ThrowRuntimeExceptionResponse")
    public void throwRuntimeException();

    /**
     * 
     */
    @WebMethod
    @RequestWrapper(localName = "throwSoapFaultException", targetNamespace = "http://server.exception.samples.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.samples.exception.client.ThrowSoapFaultException")
    @ResponseWrapper(localName = "throwSoapFaultExceptionResponse", targetNamespace = "http://server.exception.samples.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.samples.exception.client.ThrowSoapFaultExceptionResponse")
    public void throwSoapFaultException();

    /**
     * 
     * @throws UserException_Exception
     */
    @WebMethod
    @RequestWrapper(localName = "throwApplicationException", targetNamespace = "http://server.exception.samples.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.samples.exception.client.ThrowApplicationException")
    @ResponseWrapper(localName = "throwApplicationExceptionResponse", targetNamespace = "http://server.exception.samples.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.samples.exception.client.ThrowApplicationExceptionResponse")
    public void throwApplicationException()
        throws UserException_Exception
    ;

}
