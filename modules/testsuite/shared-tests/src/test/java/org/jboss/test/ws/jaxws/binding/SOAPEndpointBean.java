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
package org.jboss.test.ws.jaxws.binding;

import static jakarta.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING;

import jakarta.jws.HandlerChain;
import jakarta.jws.WebService;
import jakarta.xml.ws.BindingType;

import org.jboss.logging.Logger;

@WebService(name="SOAPEndpoint", targetNamespace="http://org.jboss.ws/jaxws/binding", 
      portName="SOAPEndpointPort",
      serviceName="SOAPEndpointService", 
      endpointInterface = "org.jboss.test.ws.jaxws.binding.SOAPEndpoint")
@BindingType(SOAP12HTTP_BINDING)
@HandlerChain(file = "jaxws-server-handlers.xml")
public class SOAPEndpointBean implements SOAPEndpoint
{
   private static Logger log = Logger.getLogger(SOAPEndpointBean.class);

   public String namespace()
   {
      //Get the nsUri in the received message that was analyzed in the server handler
      //just a trick for this test, not to be used with real world apps 
      String nsURI = ServerHandler.getNsURI();

      log.info(nsURI);

      return nsURI;
   }
}
