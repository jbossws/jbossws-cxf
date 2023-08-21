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
package org.jboss.wsf.stack.cxf.client.configuration;

public class BeanB
{

   private BeanA firstBeanA;
   
   private BeanA secondBeanA;
   
   private String name;

   private int num;

   private Long bigNumber;

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public int getNum()
   {
      return num;
   }

   public void setNum(int num)
   {
      this.num = num;
   }

   public Long getBigNumber()
   {
      return bigNumber;
   }

   public void setBigNumber(Long bigNumber)
   {
      this.bigNumber = bigNumber;
   }

   public BeanA getFirstBeanA()
   {
      return firstBeanA;
   }

   public void setFirstBeanA(BeanA firstBeanA)
   {
      this.firstBeanA = firstBeanA;
   }

   public BeanA getSecondBeanA()
   {
      return secondBeanA;
   }

   public void setSecondBeanA(BeanA secondBeanA)
   {
      this.secondBeanA = secondBeanA;
   }
}
