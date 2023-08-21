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
import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author Heiko Braun <heiko.braun@jboss.com>
 * @since Nov 7, 2006
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
   name = "orderType",
   namespace="http://org.jboss.ws/samples/retail",
   propOrder = { "orderNum", "state", "customer", "items" }
)
public class Order implements Serializable {

   private static final long serialVersionUID = -2683933732949647802L;

   public enum OrderState {TRANSIENT, PREPARED, VERIFIED, PROCESSED}

   private OrderState state;
   private long orderNum;
   private Customer customer;
   private List<OrderItem> items;

   public Order(Customer customer) {
      this.customer = customer;
   }

   public Order() {
      this.state = OrderState.TRANSIENT;
   }

   public long getOrderNum() {
      return orderNum;
   }

   public void setOrderNum(long orderNum) {
      this.orderNum = orderNum;
   }

   public Customer getCustomer() {
      return customer;
   }

   public void setCustomer(Customer customer) {
      this.customer = customer;
   }

   public List<OrderItem> getItems() {
      if(null==items)
         items = new ArrayList<OrderItem>();
      return items;
   }

   public OrderState getState() {
      return state;
   }

   public void setState(OrderState state) {
      this.state = state;
   }

   @Override
   public String toString() {
      return "Order {num="+orderNum+"}";
   }
}
