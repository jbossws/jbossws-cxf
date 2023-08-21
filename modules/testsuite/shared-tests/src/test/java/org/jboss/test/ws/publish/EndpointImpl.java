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
package org.jboss.test.ws.publish;

import jakarta.annotation.Resource;
import jakarta.jws.WebService;
import jakarta.xml.ws.WebServiceContext;
import jakarta.xml.ws.handler.MessageContext;

import org.jboss.logging.Logger;

@WebService(serviceName="EndpointService", portName="EndpointPort", endpointInterface = "org.jboss.test.ws.publish.Endpoint")
public class EndpointImpl
{
   @Resource
   WebServiceContext wsCtx;
   
   // Provide logging
   private static Logger log = Logger.getLogger(EndpointImpl.class);

   public String echo(String input)
   {
      log.info("echo: " + input);
      MessageContext msgContext = (MessageContext)wsCtx.getMessageContext();
      if (msgContext == null) {
         return "MessageContext is null!";
      }
      log.info("WSDL_DESCRIPTION: " + msgContext.get(MessageContext.WSDL_DESCRIPTION));
      log.info("WSDL_SERVICE: " + msgContext.get(MessageContext.WSDL_SERVICE));
      log.info("WSDL_INTERFACE: " + msgContext.get(MessageContext.WSDL_INTERFACE));
      log.info("WSDL_PORT: " + msgContext.get(MessageContext.WSDL_PORT));
      log.info("WSDL_OPERATION: " + msgContext.get(MessageContext.WSDL_OPERATION));
      return input;
   }
}
