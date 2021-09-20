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
package org.jboss.test.ws.jaxws.jbws3552;

import jakarta.xml.bind.annotation.XmlTransient;

@XmlTransient
public class AdaptedExceptionCA extends Exception {

   private static final long serialVersionUID = 3891004410967817L;
   private String message;
   private String description;
   private ComplexObjectCA complexObject;

   public AdaptedExceptionCA() {
      super();
   }

   public AdaptedExceptionCA(String message, String description, ComplexObjectCA complexObject) {
      this.message = message;
      this.description = description;
      this.complexObject = complexObject;
   }

   @Override
   public String getMessage() {
      return message;
   }

   public String getDescription() {
      return description;
   }

   public ComplexObjectCA getComplexObject() {
      return complexObject;
   }

   @Override
   public String toString() {
      return message + "," + description + "," + complexObject;
   }
}