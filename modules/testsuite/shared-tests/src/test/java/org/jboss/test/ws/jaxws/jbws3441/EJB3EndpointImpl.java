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
package org.jboss.test.ws.jaxws.jbws3441;

import jakarta.ejb.Stateless;
import jakarta.jws.WebService;

/**
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
@WebService(name = "EJB3Endpoint", serviceName="EJB3EndpointService", targetNamespace = "http://org.jboss.test.ws/jbws3441")
@Stateless
public class EJB3EndpointImpl implements EndpointIface
{
   static boolean interceptorCalled;

   @EJBInterceptor
   public String echo(final String message)
   {
      return interceptorCalled ? message + " (including EJB interceptor)" : message;
   }
}
