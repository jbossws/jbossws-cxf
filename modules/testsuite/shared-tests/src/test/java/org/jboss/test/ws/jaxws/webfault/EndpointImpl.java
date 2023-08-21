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
package org.jboss.test.ws.jaxws.webfault;

import jakarta.jws.WebService;

import org.jboss.logging.Logger;

/**
 * Test the JSR-181 annotation: jakarta.jws.WebFault
 *
 * @author alessio.soldano@jboss.org
 * @since 21-Feb-2008
 */
@WebService(serviceName="EndpointService", endpointInterface = "org.jboss.test.ws.jaxws.webfault.Endpoint")
public class EndpointImpl
{
   // Provide logging
   private static Logger log = Logger.getLogger(EndpointImpl.class);

   public void throwCustomException(String input) throws CustomException
   {
      log.info("throwCustomException: " + input);
      throw new CustomException("This is a @WebFault test", (input != null ? input.length() : 0), "blah, blah");
   }
   
   public void throwSimpleException(String input) throws SimpleException
   {
      log.info("throwSimpleException: " + input);
      throw new SimpleException("This is a @WebFault test", (input != null ? input.length() : 0));
   }
}
