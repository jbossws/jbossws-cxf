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
package org.jboss.test.ws.jaxws.jbws3870;

import java.util.ArrayList;
import java.util.List;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.xml.ws.RequestWrapper;
import jakarta.xml.ws.ResponseWrapper;

@WebService(targetNamespace = "http://apache.org/sayHi", name = "SayHi", wsdlLocation = "WEB-INF/classes/wsdl/thewsdl/sayHi.wsdl", endpointInterface = "org.jboss.test.ws.jaxws.jbws3870.SayHi")
public class SayHiImpl implements SayHi
{

   @Override
   @WebResult(name = "return", targetNamespace = "")
   @RequestWrapper(localName = "sayHiArray", targetNamespace = "http://apache.org/sayHi2", className = "org.jboss.sayhi2.SayHiArray")
   @WebMethod
   @ResponseWrapper(localName = "sayHiArrayResponse", targetNamespace = "http://apache.org/sayHi2", className = "org.jboss.sayhi2.SayHiArrayResponse")
   public List<String> sayHiArray(@WebParam(name = "arg0", targetNamespace = "") List<String> arg0)
   {
      List<String> list = new ArrayList<String>();
      list.add("Hi");
      return list;
   }

   @Override
   @WebResult(name = "return", targetNamespace = "http://apache.org/sayHi1")
   @RequestWrapper(localName = "sayHi", targetNamespace = "http://apache.org/sayHi1", className = "org.jboss.sayhi1.SayHi")
   @WebMethod
   @ResponseWrapper(localName = "sayHiResponse", targetNamespace = "http://apache.org/sayHi1", className = "org.jboss.sayhi1.SayHiResponse")
   public String sayHi(@WebParam(name = "arg0", targetNamespace = "http://apache.org/sayHi1") String arg0)
   {
      return "Hi";
   }

}
