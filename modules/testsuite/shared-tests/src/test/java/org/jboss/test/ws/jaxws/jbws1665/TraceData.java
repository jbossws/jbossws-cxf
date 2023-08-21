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
