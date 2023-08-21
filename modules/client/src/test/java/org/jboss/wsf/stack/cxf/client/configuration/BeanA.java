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

public class BeanA
{

   private String name;

   private int num;

   private Long bigNumber;
   
   private BeanB beanB;
   
   private boolean pretty;

   public boolean isPretty()
   {
      return pretty;
   }

   public void setPretty(boolean pretty)
   {
      this.pretty = pretty;
   }

   public BeanB getBeanB()
   {
      return beanB;
   }

   public void setBeanB(BeanB beanB)
   {
      this.beanB = beanB;
   }

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
}
