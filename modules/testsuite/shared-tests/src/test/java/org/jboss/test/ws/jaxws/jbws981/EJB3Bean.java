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
package org.jboss.test.ws.jaxws.jbws981;

import jakarta.ejb.Stateless;
import jakarta.jws.WebService;

import org.jboss.ws.api.annotation.WebContext;

/**
 *
 * @author darran.lofthouse@jboss.com
 * @since Nov 2, 2006
 */
@Stateless
@WebService(endpointInterface = "org.jboss.test.ws.jaxws.jbws981.EndpointInterface", targetNamespace = "http://www.jboss.org/test/ws/jaxws/jbws981", serviceName = "EndpointService")
@WebContext(virtualHost = "default-host")
public class EJB3Bean implements EJB3RemoteInterface
{

   public String hello(final String message)
   {
      return message;
   }

}
