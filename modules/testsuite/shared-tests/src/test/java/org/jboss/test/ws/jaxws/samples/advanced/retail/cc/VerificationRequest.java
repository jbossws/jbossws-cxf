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
package org.jboss.test.ws.jaxws.samples.advanced.retail.cc;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

/**
 * <p>Java class for verificationRequest complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="verificationRequest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="creditCardNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "verificationRequest", propOrder = {
    "creditCardNumber"
    })
public class VerificationRequest {

   protected String creditCardNumber;

   public VerificationRequest(String creditCardNumber) {
      this.creditCardNumber = creditCardNumber;
   }

   public VerificationRequest() {
   }

   /**
    * Gets the value of the creditCardNumber property.
    *
    * @return
    *     possible object is
    *     {@link String }
    *
    */
   public String getCreditCardNumber() {
      return creditCardNumber;
   }

   /**
    * Sets the value of the creditCardNumber property.
    *
    * @param value
    *     allowed object is
    *     {@link String }
    *
    */
   public void setCreditCardNumber(String value) {
      this.creditCardNumber = value;
   }

}
