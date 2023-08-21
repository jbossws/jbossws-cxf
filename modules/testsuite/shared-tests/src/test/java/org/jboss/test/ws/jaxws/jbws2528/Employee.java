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
package org.jboss.test.ws.jaxws.jbws2528;

public class Employee
{

   private Name name;
   private String dept;
   private Float salary;
   private String address;
   private String title;
   private int type;

   public Employee(Name name, String dept, Float salary, String address, String title, int type)
   {
      this.name = name;
      this.dept = dept;
      this.salary = salary;
      this.address = address;
      this.title = title;
      this.type = type;
   }

   public Employee()
   {
      name = new Name();
      dept = "";
      salary = 0F;
      address = "";
      title = "";
      type = EmployeeType.PERMANENT;
   }

   public String toString()
   {
      return name + "::" + dept + "::" + salary + "::" + address + "::" + title + "::" + type;
   }

   public Name getName()
   {
      return name;
   }

   public void setName(Name name)
   {
      this.name = name;
   }

   public String getDept()
   {
      return dept;
   }

   public void setDept(String dept)
   {
      this.dept = dept;
   }

   public Float getSalary()
   {
      return salary;
   }

   public void setSalary(Float salary)
   {
      this.salary = salary;
   }

   public String getAddress()
   {
      return address;
   }

   public void setAddress(String address)
   {
      this.address = address;
   }

   public String getTitle()
   {
      return title;
   }

   public void setTitle(String title)
   {
      this.title = title;
   }

   public int getType()
   {
      return type;
   }

   public void setType(int type)
   {
      this.type = type;
   }

}
