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
package org.jboss.test.ws.jaxws.benchmark.test.datatypes.types;

import java.util.Arrays;

import java.util.Date;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

/**
 * @author pmacik@redhat.com
 * @since 09-Mar-2010
 */
public class ComplexType
{
   private byte byteValue;

   private byte[] byteArrayValue;

   private short shortValue;

   private int intValue;

   private long longValue;

   private float floatValue;

   private double doubleValue;

   private String stringValue;

   private List<String> stringList;

   private QName qNameValue;

   private Date dateValue;

   private XMLGregorianCalendar calendarValue;

   public ComplexType()
   {
      super();
   }

   public ComplexType(byte byteValue, byte[] byteArrayValue, short shortValue, int intValue, long longValue,
         float floatValue, double doubleValue, String stringValue, List<String> stringList, QName qNameValue,
         Date dateValue, XMLGregorianCalendar calendarValue)
   {
      super();
      this.byteValue = byteValue;
      this.byteArrayValue = byteArrayValue;
      this.shortValue = shortValue;
      this.intValue = intValue;
      this.longValue = longValue;
      this.floatValue = floatValue;
      this.doubleValue = doubleValue;
      this.stringValue = stringValue;
      this.stringList = stringList;
      this.qNameValue = qNameValue;
      this.dateValue = dateValue;
      this.calendarValue = calendarValue;
   }

   public byte getByteValue()
   {
      return byteValue;
   }

   public void setByteValue(byte byteValue)
   {
      this.byteValue = byteValue;
   }

   public byte[] getByteArrayValue()
   {
      return byteArrayValue;
   }

   public void setByteArrayValue(byte[] byteArrayValue)
   {
      this.byteArrayValue = byteArrayValue;
   }

   public short getShortValue()
   {
      return shortValue;
   }

   public void setShortValue(short shortValue)
   {
      this.shortValue = shortValue;
   }

   public int getIntValue()
   {
      return intValue;
   }

   public void setIntValue(int intValue)
   {
      this.intValue = intValue;
   }

   public long getLongValue()
   {
      return longValue;
   }

   public void setLongValue(long longValue)
   {
      this.longValue = longValue;
   }

   public float getFloatValue()
   {
      return floatValue;
   }

   public void setFloatValue(float floatValue)
   {
      this.floatValue = floatValue;
   }

   public double getDoubleValue()
   {
      return doubleValue;
   }

   public void setDoubleValue(double doubleValue)
   {
      this.doubleValue = doubleValue;
   }

   public String getStringValue()
   {
      return stringValue;
   }

   public void setStringValue(String stringValue)
   {
      this.stringValue = stringValue;
   }

   public List<String> getStringList()
   {
      return stringList;
   }

   public void setStringList(List<String> stringList)
   {
      this.stringList = stringList;
   }

   public QName getqNameValue()
   {
      return qNameValue;
   }

   public void setqNameValue(QName qNameValue)
   {
      this.qNameValue = qNameValue;
   }

   public Date getDateValue()
   {
      return dateValue;
   }

   public void setDateValue(Date dateValue)
   {
      this.dateValue = dateValue;
   }

   public XMLGregorianCalendar getCalendarValue()
   {
      return calendarValue;
   }

   public void setCalendarValue(XMLGregorianCalendar calendarValue)
   {
      this.calendarValue = calendarValue;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + Arrays.hashCode(byteArrayValue);
      result = prime * result + byteValue;
      result = prime * result + ((calendarValue == null) ? 0 : calendarValue.hashCode());
      result = prime * result + ((dateValue == null) ? 0 : dateValue.hashCode());
      long temp;
      temp = Double.doubleToLongBits(doubleValue);
      result = prime * result + (int) (temp ^ (temp >>> 32));
      result = prime * result + Float.floatToIntBits(floatValue);
      result = prime * result + intValue;
      result = prime * result + (int) (longValue ^ (longValue >>> 32));
      result = prime * result + ((qNameValue == null) ? 0 : qNameValue.hashCode());
      result = prime * result + shortValue;
      result = prime * result + ((stringList == null) ? 0 : stringList.hashCode());
      result = prime * result + ((stringValue == null) ? 0 : stringValue.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      ComplexType other = (ComplexType) obj;
      if (!Arrays.equals(byteArrayValue, other.byteArrayValue))
         return false;
      if (byteValue != other.byteValue)
         return false;
      if (calendarValue == null)
      {
         if (other.calendarValue != null)
            return false;
      }
      else if (!calendarValue.equals(other.calendarValue))
         return false;
      if (dateValue == null)
      {
         if (other.dateValue != null)
            return false;
      }
      else if (!dateValue.equals(other.dateValue))
         return false;
      if (Double.doubleToLongBits(doubleValue) != Double.doubleToLongBits(other.doubleValue))
         return false;
      if (Float.floatToIntBits(floatValue) != Float.floatToIntBits(other.floatValue))
         return false;
      if (intValue != other.intValue)
         return false;
      if (longValue != other.longValue)
         return false;
      if (qNameValue == null)
      {
         if (other.qNameValue != null)
            return false;
      }
      else if (!qNameValue.equals(other.qNameValue))
         return false;
      if (shortValue != other.shortValue)
         return false;
      if (stringList == null)
      {
         if (other.stringList != null)
            return false;
      }
      else if (!stringList.equals(other.stringList))
         return false;
      if (stringValue == null)
      {
         if (other.stringValue != null)
            return false;
      }
      else if (!stringValue.equals(other.stringValue))
         return false;
      return true;
   }

   @Override
   public String toString()
   {
      return "ComplexType [byteArrayValue=" + Arrays.toString(byteArrayValue) + ", byteValue=" + byteValue
            + ", calendarValue=" + calendarValue + ", dateValue=" + dateValue + ", doubleValue=" + doubleValue
            + ", floatValue=" + floatValue + ", intValue=" + intValue + ", longValue=" + longValue + ", qNameValue="
            + qNameValue + ", shortValue=" + shortValue + ", stringList=" + stringList + ", stringValue=" + stringValue
            + "]";
   }

}
