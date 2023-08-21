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
package org.jboss.test.ws.jaxws.jbws1190;

import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;

/**
 If the implementation bean does not implement a service endpoint interface and
 there are no @WebMethod annotations in the implementation bean (excluding
 @WebMethod annotations used to exclude inherited @WebMethods), all public
 methods other than those inherited from java.lang.Object will be exposed as Web
 Service operations, subject to the inheritance rules specified in Common
 Annotations for the Java Platform [12], section 2.1.
 */
@WebService(serviceName = "ConfidentialService", targetNamespace = "http://org.jboss/test/ws/jbws1190")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class ConfidentialEndpoint
{
   // Intentionally no @WebMethod, see above
   public String helloWorld(final String message)
   {
      return message;
   }
}
