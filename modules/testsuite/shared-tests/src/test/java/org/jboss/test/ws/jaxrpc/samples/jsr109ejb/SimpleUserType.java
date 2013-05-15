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
package org.jboss.test.ws.jaxrpc.samples.jsr109ejb;

/**
 * @author Thomas.Diesler@jboss.org
 * @since 23-Jan-2005
 */
public class SimpleUserType
{
   public int a;
   private int b;

   public SimpleUserType()
   {
   }

   public SimpleUserType(int a, int b)
   {
      this.a = a;
      this.b = b;
   }

   public int getB()
   {
      return b;
   }

   public void setB(int b)
   {
      this.b = b;
   }

   public boolean equals(Object o)
   {
      if (this == o) return true;
      if (!(o instanceof SimpleUserType)) return false;

      final SimpleUserType simpleUserType = (SimpleUserType)o;

      if (a != simpleUserType.a) return false;
      if (b != simpleUserType.b) return false;

      return true;
   }

   public int hashCode()
   {
      int result;
      result = a;
      result = 29 * result + b;
      return result;
   }

   public String toString()
   {
      return "[a=" + a + ",b=" + b + "]";
   }
}
