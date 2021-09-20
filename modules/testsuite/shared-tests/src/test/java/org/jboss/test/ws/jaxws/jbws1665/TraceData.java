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
package org.jboss.test.ws.jaxws.jbws1665;

import java.io.Serializable;
import java.util.Calendar;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TraceData", propOrder = { "type", "source", "time",
      "coordinate", "mileage", "heading", "speed", "property" })
public class TraceData implements Serializable {
   
   private static final long serialVersionUID = 1556686846129761858L;
   
   @XmlElement(required=true)
   private short type;
   @XmlElement(required=true)
   private String source;
   @XmlElement(required=true)
   private Calendar time;
   private CoordinateData coordinate;
   private Integer mileage;
   private Short heading;
   private Short speed;
   private PropertyData[] property;
   
   public CoordinateData getCoordinate() {
      return coordinate;
   }

   public void setCoordinate(CoordinateData coordinate) {
      this.coordinate = coordinate;
   }

   public Short getHeading() {
      return heading;
   }

   public void setHeading(Short heading) {
      this.heading = heading;
   }

   public Integer getMileage() {
      return mileage;
   }

   public void setMileage(Integer mileage) {
      this.mileage = mileage;
   }

   public String getSource() {
      return source;
   }

   public void setSource(String source) {
      this.source = source;
   }

   public Short getSpeed() {
      return speed;
   }

   public void setSpeed(Short speed) {
      this.speed = speed;
   }

   public Calendar getTime() {
      return time;
   }

   public void setTime(Calendar time) {
      this.time = time;
   }

   public short getType() {
      return type;
   }

   public void setType(short type) {
      this.type = type;
   }
   
   public void setProperty(PropertyData[] property) {
      this.property = property;
   }

   public PropertyData[] getProperty() {
      return property;
   }
}
