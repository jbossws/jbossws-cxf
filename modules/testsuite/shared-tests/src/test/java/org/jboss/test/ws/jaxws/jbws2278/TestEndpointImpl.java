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
package org.jboss.test.ws.jaxws.jbws2278;


/**
 * Test Endpoint implementation.
 * 
 * @author alessio.soldano@jboss.com
 * @since 30-Sep-2008
 */
public class TestEndpointImpl
{

   public static final String TEST_EXCEPTION = "TestException";

   public static final String RUNTIME_EXCEPTION = "RuntimeException";

   public String echo(String message) throws TestException_Exception
   {
      if (TEST_EXCEPTION.equals(message))
      {
         TestException te = new TestException();
         throw new TestException_Exception(message, te);
      }
      else if (RUNTIME_EXCEPTION.equals(message))
      {
         throw new RuntimeException("Simulated failure");
      }
      return message;
   }

}
