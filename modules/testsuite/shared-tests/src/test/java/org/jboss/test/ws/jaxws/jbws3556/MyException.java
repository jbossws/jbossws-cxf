/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws3556;

public class MyException extends Exception {

   private static final long serialVersionUID = -1349782382698409653L;
   private String from;
   private int id;
   private String summary;

   public MyException() {} // mandatory constructor

   public MyException(String from, int id, String message, String summary) {
      super(message);
      this.from = from;
      this.id = id;
      this.summary = summary;
   }

   // mandatory from setter
   public void setFrom(String from) {
      this.from = from;
   }

    // mandatory id setter
   public void setId(int id) {
      this.id = id;
   }

   // mandatory summary setter
   public void setSummary(String summary) {
      this.summary = summary;
   }

   public String getFrom() {
      return from;
   }

   public int getId() {
      return id;
   }

   public String getSummary() {
      return summary;
   }

   @Override
   public String toString() {
      return from + "," + id + "," + getMessage() + "," + summary;
   }
}