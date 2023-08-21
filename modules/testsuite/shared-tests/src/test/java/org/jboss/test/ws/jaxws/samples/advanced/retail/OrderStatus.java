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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author Heiko Braun <heiko.braun@jboss.com>
 * @since Nov 8, 2006
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "orderStatus", namespace = "http://org.jboss.ws/samples/retail")
public class OrderStatus implements Serializable {

   private static final long serialVersionUID = 2140710606232315955L;
   private String status;
   private long orderNum;
   private double discount;

   public OrderStatus() {
   }

   public OrderStatus(String status, long orderNum, double discount) {
      this.status = status;
      this.orderNum = orderNum;
      this.discount = discount;
   }

   public String getStatus() {
      return status;
   }

   public void setStatus(String status) {
      this.status = status;
   }

   public long getOrderNum() {
      return orderNum;
   }

   public void setOrderNum(long orderNum) {
      this.orderNum = orderNum;
   }

   public double getDiscount() {
      return discount;
   }

   public void setDiscount(double discount) {
      this.discount = discount;
   }
}
