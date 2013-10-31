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

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlTransient
public class AdaptedExceptionMA extends Exception {
   private static final long serialVersionUID = -50115093835509488L;
   private String message;
   private String description;
   private ComplexObjectMA complexObject;

   public AdaptedExceptionMA() {
      super();
   }

   public AdaptedExceptionMA(String message, String description, ComplexObjectMA complexObject) {
      this.message = message;
      this.description = description;
      this.complexObject = complexObject;
   }

   public void setMessage(String message) {
      this.message = message;
   }

   @Override
   public String getMessage() {
      return message;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public String getDescription() {
      return description;
   }

   public void setComplexObject(ComplexObjectMA complexObject) {
      this.complexObject = complexObject;
   }

   @XmlJavaTypeAdapter(value = ComplexObjectMAAdapter.class)
   public ComplexObjectMA getComplexObject() {
      return complexObject;
   }

   @Override
   public String toString() {
      return message + "," + description + "," + complexObject;
   }
}