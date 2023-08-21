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

package org.jboss.test.ws.jaxws.jbws2257;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.jws.WebService;
import jakarta.xml.ws.EndpointReference;
import jakarta.xml.ws.WebServiceContext;

import org.jboss.logging.Logger;

@Stateless
@WebService
(
   portName = "AddressingServicePort",
   serviceName = "AddressingService",
   wsdlLocation = "META-INF/wsdl/AddressingService.wsdl",
   targetNamespace = "http://www.jboss.org/jbossws/ws-extensions/wsaddressing",
   endpointInterface = "org.jboss.test.ws.jaxws.jbws2257.ServiceIface"
)
public class ServiceImpl
{
   private static final Logger log = Logger.getLogger(ServiceImpl.class);
   
   @Resource
   WebServiceContext ctx;
   
   public String sayHello()
   {
      log.info("Current context: " + ctx);
      try
      {
         EndpointReference epr = ctx.getEndpointReference();
         log.info("Endpoint reference: " + epr);
         if (epr == null || !epr.toString().contains("jbws2257"))
         {
            return "Unexpected endpoint reference: " + epr;
         }
      }
      catch (Exception e)
      {
         log.error("Error while reading endpoint reference from context!", e);
         return e.getMessage();
      }
      return "Hello World!";
   }
}
