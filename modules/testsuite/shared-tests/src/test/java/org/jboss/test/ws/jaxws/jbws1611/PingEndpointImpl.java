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
package org.jboss.test.ws.jaxws.jbws1611;

import jakarta.annotation.Resource;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.xml.ws.Action;
import jakarta.xml.ws.WebServiceContext;
import jakarta.xml.ws.handler.MessageContext;

import org.jboss.logging.Logger;

@WebService(name = "PingEndpoint", serviceName = "PingEndpointService")
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT, parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
public class PingEndpointImpl
{
   // Provide logging
   private static Logger log = Logger.getLogger(PingEndpointImpl.class);

   @Resource
   WebServiceContext wsctx;

   @Action(input="uri:placeBuyOrder")
   @WebMethod
   public String ping()
   {
      HttpServletRequest req = (HttpServletRequest)wsctx.getMessageContext().get(MessageContext.SERVLET_REQUEST);
      String soapAction = req.getHeader("SOAPAction");
      log.info("ping: " + soapAction);
      return soapAction;
   }
}
