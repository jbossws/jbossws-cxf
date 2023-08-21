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
package org.jboss.test.ws.jaxws.samples.advanced.retail.profile;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

import org.jboss.test.ws.jaxws.samples.advanced.retail.Customer;

/**
 * <p>Java class for discountResponse complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="discountResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="customer" type="{http://org.jboss.ws/samples/retail}customer" minOccurs="0"/>
 *         &lt;element name="discount" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "discountResponse", propOrder = {
    "customer",
    "discount"
    })
public class DiscountResponse {

   protected Customer customer;
   protected double discount;

   public DiscountResponse() {
   }

   public DiscountResponse(Customer customer, double discount) {
      this.customer = customer;
      this.discount = discount;
   }

   /**
    * Gets the value of the customer property.
    *
    * @return
    *     possible object is
    *     {@link Customer }
    *
    */
   public Customer getCustomer() {
      return customer;
   }

   /**
    * Sets the value of the customer property.
    *
    * @param value
    *     allowed object is
    *     {@link Customer }
    *
    */
   public void setCustomer(Customer value) {
      this.customer = value;
   }

   /**
    * Gets the value of the discount property.
    *
    */
   public double getDiscount() {
      return discount;
   }

   /**
    * Sets the value of the discount property.
    *
    */
   public void setDiscount(double value) {
      this.discount = value;
   }

}
