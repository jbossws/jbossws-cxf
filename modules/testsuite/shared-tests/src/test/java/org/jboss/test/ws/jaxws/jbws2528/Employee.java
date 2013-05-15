/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
