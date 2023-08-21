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
package org.jboss.test.ws.jaxws.samples.webresult;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;

import org.jboss.logging.Logger;

/**
 * Test the JSR-181 annotation: jakarta.jws.WebParam
 *
 * @author Thomas.Diesler@jboss.org
 * @since 07-Oct-2005
 */
@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class CustomerServiceImpl
{
   // Provide logging
   private static Logger log = Logger.getLogger(CustomerServiceImpl.class);

   @WebMethod
   @WebResult(name = "CustomerRecord")
   public CustomerRecord locateCustomer(
         @WebParam(name = "FirstName") String firstName, 
         @WebParam(name = "LastName") String lastName, 
         @WebParam(name = "Address") USAddress addr)
   {
      CustomerRecord rec = new CustomerRecord();
      rec.setFirstName(firstName);
      rec.setLastName(lastName);
      rec.setAddress(addr);
      log.info("locateCustomer: " + rec);
      return rec;
   }
}
