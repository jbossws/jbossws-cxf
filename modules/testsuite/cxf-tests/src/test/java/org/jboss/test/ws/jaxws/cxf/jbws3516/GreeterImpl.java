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

import jakarta.jws.WebService;
import jakarta.xml.ws.soap.Addressing;
import org.apache.cxf.interceptor.InInterceptors;

@WebService(serviceName = "SOAPService", portName = "SoapPort", 
            endpointInterface = "org.jboss.test.ws.jaxws.cxf.jbws3516.Greeter", 
            targetNamespace = "http://jboss.org/hello_world", 
            wsdlLocation = "WEB-INF/wsdl/hello_world.wsdl")
@Addressing
@InInterceptors(interceptors = { "org.apache.cxf.ws.addressing.soap.DecoupledFaultHandler" })
public class GreeterImpl implements Greeter
{
   public String sayHi(String request) throws SayHiFault
   {
      if (request.equals("fault"))
      {
         throw new SayHiFault("Intended SayHi Fault");
      }
      return request;
   }

   public void pingMe()
   {
      throw new RuntimeException("Intended PingMe Fault");
   }

}
