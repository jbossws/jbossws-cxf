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
package org.jboss.test.ws.jaxws.samples.soapbinding;

import jakarta.jws.WebService;

import org.jboss.logging.Logger;

/**
 * Test the JSR-181 annotation: jakarta.jws.SOAPBinding
 *
 * @author Thomas.Diesler@jboss.org
 * @since 16-Oct-2005
 */

@WebService(serviceName="ExampleService", endpointInterface="org.jboss.test.ws.jaxws.samples.soapbinding.ExampleSEI")
public class ExampleServiceImpl
{
   // Provide logging
   private static Logger log = Logger.getLogger(ExampleServiceImpl.class);

   public String concat(String first, String second, String third)
   {
      String retStr = first + "|" + second + "|" + third;
      log.info("concat: " + retStr);
      return retStr;
   }
}
