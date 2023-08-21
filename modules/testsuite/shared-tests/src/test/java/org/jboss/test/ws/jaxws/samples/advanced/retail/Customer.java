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
package org.jboss.test.ws.jaxws.samples.advanced.retail;

import java.io.Serializable;

import jakarta.xml.bind.annotation.XmlType;

@XmlType(name="customer", namespace = "http://org.jboss.ws/samples/retail", propOrder = {
    "creditCardDetails",
    "firstName",
    "lastName"
})
public class Customer implements Serializable {

   private static final long serialVersionUID = 8072660329498454521L;
   private String firstName;
   private String lastName;
   private String creditCardDetails;

   public String getFirstName() {
      return firstName;
   }

   public void setFirstName(String firstName) {
      this.firstName = firstName;
   }

   public String getLastName() {
      return lastName;
   }

   public void setLastName(String lastName) {
      this.lastName = lastName;
   }

   public String getCreditCardDetails() {
      return creditCardDetails;
   }

   public void setCreditCardDetails(String creditCardDetails) {
      this.creditCardDetails = creditCardDetails;
   }
}

