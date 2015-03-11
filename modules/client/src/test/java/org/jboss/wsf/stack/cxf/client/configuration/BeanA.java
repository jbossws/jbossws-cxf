/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
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
