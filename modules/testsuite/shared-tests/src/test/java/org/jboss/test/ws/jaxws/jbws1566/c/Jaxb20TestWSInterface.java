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
package org.jboss.test.ws.jaxws.jbws1566.c;

import java.rmi.RemoteException;

import org.jboss.test.ws.jaxws.jbws1566.a.TestEnumeration;
import org.jboss.test.ws.jaxws.jbws1566.b.BClass;
import org.jboss.test.ws.jaxws.jbws1566.b.BException;

@jakarta.jws.WebService(targetNamespace = "http://org.jboss.ws/samples/c")
@jakarta.jws.soap.SOAPBinding(style = jakarta.jws.soap.SOAPBinding.Style.DOCUMENT, use = jakarta.jws.soap.SOAPBinding.Use.LITERAL, parameterStyle = jakarta.jws.soap.SOAPBinding.ParameterStyle.WRAPPED)
public interface Jaxb20TestWSInterface extends java.rmi.Remote
{

   @jakarta.jws.WebMethod(operationName = "Method")
   public @jakarta.jws.WebResult(name = "result")
   TestEnumeration testMethod(@jakarta.jws.WebParam(name = "input", mode = jakarta.jws.WebParam.Mode.IN)
   BClass input) throws BException, RemoteException;

}
