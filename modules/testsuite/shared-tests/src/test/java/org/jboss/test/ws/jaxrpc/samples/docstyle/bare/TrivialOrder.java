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
package org.jboss.test.ws.jaxrpc.samples.docstyle.bare;

/**
 * @author Thomas.Diesler@jboss.org
 * @since 04-Jun-2005
 */
public class TrivialOrder
{
   private String person;
   private String product;

   public TrivialOrder()
   {
   }

   public TrivialOrder(String person, String product)
   {
      this.person = person;
      this.product = product;
   }

   public String getPerson()
   {
      return person;
   }

   public void setPerson(String person)
   {
      this.person = person;
   }

   public String getProduct()
   {
      return product;
   }

   public void setProduct(String product)
   {
      this.product = product;
   }

   public boolean equals(Object o)
   {
      if (this == o) return true;
      if (!(o instanceof TrivialOrder)) return false;

      final TrivialOrder trivialOrder = (TrivialOrder)o;

      if (person != null ? !person.equals(trivialOrder.person) : trivialOrder.person != null) return false;
      if (product != null ? !product.equals(trivialOrder.product) : trivialOrder.product != null) return false;

      return true;
   }

   public int hashCode()
   {
      int result;
      result = (person != null ? person.hashCode() : 0);
      result = 29 * result + (product != null ? product.hashCode() : 0);
      return result;
   }

   public String toString()
   {
      return "[person=" + person + ",product=" + product + "]";
   }
}
