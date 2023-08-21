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
package org.jboss.test.ws.jaxws.samples.webserviceref;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import jakarta.jws.soap.SOAPBinding.Style;

import org.jboss.logging.Logger;

@WebService(name = "Endpoint", serviceName = "EndpointService", targetNamespace = "http://org.jboss.ws/wsref")
@SOAPBinding(style = Style.RPC)
public class EndpointImpl
{
   // Provide logging
   private static Logger log = Logger.getLogger(EndpointImpl.class);

   @WebMethod
   public String echo(String input)
   {
      log.info(input);
      return input;
   }
}
