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
package org.jboss.test.ws.jaxws.benchmark.test.datatypes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import jakarta.jws.WebService;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.jboss.test.ws.jaxws.benchmark.test.datatypes.types.ComplexType;

/**
 * @author pmacik@redhat.com
 * @since 09-Mar-2010
 */
@WebService(serviceName = "EndpointWrappedRPCService", portName = "EndpointWrappedRPCPort", endpointInterface = "org.jboss.test.ws.jaxws.benchmark.test.datatypes.EndpointWrappedRPC")
public class EndpointPOJOWrappedRPCImpl implements EndpointRPC
{
   private static Duration dayDuration;
   static
   {
      try
      {
         dayDuration = DatatypeFactory.newInstance().newDuration(86400000L);
      }
      catch (DatatypeConfigurationException e)
      {
         dayDuration = null;
         e.printStackTrace();
      }
   }

   public String sayHello(String toWhom)
   {
      String greeting = "Hello World Greeting for '" + toWhom + "' today";
      return greeting;
   }

   public BigDecimal increaseBigDecimal(BigDecimal value)
   {
      return value.add(new BigDecimal(1));
   }

   public boolean negateBoolean(boolean value)
   {
      return !value;
   }

   public byte increaseByte(byte value)
   {
      return (byte) (value + 1);
   }

   public byte[] reverseByteOrder(byte[] data)
   {
      byte[] retVal = new byte[data.length];
      for (int i = 0; i < data.length; i++)
      {
         retVal[i] = data[data.length - 1 - i];
      }
      return retVal;
   }

   public XMLGregorianCalendar getCalendarPlusDay(XMLGregorianCalendar calendar)
   {
      calendar.add(dayDuration);
      return (XMLGregorianCalendar) calendar.clone();
   }

   public Date getDatePlusDay(Date date)
   {
      return new Date(date.getTime() + 86400000L);
   }

   public double increaseDouble(double value)
   {
      return value + 1;
   }

   public float increaseFloat(float value)
   {
      return value + 1;
   }

   public int increaseInt(int value)
   {
      return value + 1;
   }

   public long increaseLong(long value)
   {
      return value + 1L;
   }

   public QName modifyQName(QName value)
   {
      String modString = "_modified";
      String prefix = value.getPrefix();
      String uri = value.getNamespaceURI();
      String localPart = value.getLocalPart() + modString;
      QName retVal = new QName(uri, localPart, prefix);
      return retVal;
   }

   public short increaseShort(short value)
   {
      return (short) (value + 1);
   }

   public String valuesToString(byte byteValue, byte[] byteArrayValue, short shortValue, int intValue, long longValue,
         float floatValue, double doubleValue, String stringValue, QName qNameValue, Date dateValue,
         XMLGregorianCalendar calendarValue)
   {
      StringBuffer sb = new StringBuffer();
      sb.append("[byteValue=" + byteValue);
      sb.append(" byteArrayValue=" + new String(byteArrayValue));
      sb.append(" shortValue=" + shortValue);
      sb.append(" intValue=" + intValue);
      sb.append(" longValue=" + longValue);
      sb.append(" floatValue=" + floatValue);
      sb.append(" doubleValue=" + doubleValue);
      sb.append(" stringValue=" + stringValue);
      sb.append(" qNameValue=" + qNameValue);
      sb.append(" dateValue=" + dateValue);
      sb.append(" calendarValue=" + calendarValue);
      sb.append("]");
      return sb.toString();
   }

   public ComplexType valuesToComplexType(byte byteValue, byte[] byteArrayValue, short shortValue, int intValue,
         long longValue, float floatValue, double doubleValue, String stringValue, QName qNameValue, Date dateValue,
         XMLGregorianCalendar calendarValue)
   {
      List<String> stringList = new ArrayList<String>(11);
      stringList.add(String.valueOf(byteValue));
      stringList.add(Arrays.toString(byteArrayValue));
      stringList.add(String.valueOf(shortValue));
      stringList.add(String.valueOf(intValue));
      stringList.add(String.valueOf(longValue));
      stringList.add(String.valueOf(floatValue));
      stringList.add(String.valueOf(doubleValue));
      stringList.add(stringValue);
      stringList.add(qNameValue.toString());
      stringList.add(dateValue.toString());
      stringList.add(calendarValue.toString());
      return new ComplexType(byteValue, byteArrayValue, shortValue, intValue, longValue, floatValue, doubleValue,
            stringValue, stringList, qNameValue, dateValue, calendarValue);
   }

   public ComplexType modifyComplexType(ComplexType complexTypeValue)
   {
      complexTypeValue.setByteValue(increaseByte(complexTypeValue.getByteValue()));
      complexTypeValue.setByteArrayValue(reverseByteOrder(complexTypeValue.getByteArrayValue()));
      complexTypeValue.setShortValue(increaseShort(complexTypeValue.getShortValue()));
      complexTypeValue.setIntValue(increaseInt(complexTypeValue.getIntValue()));
      complexTypeValue.setLongValue(increaseLong(complexTypeValue.getLongValue()));
      complexTypeValue.setFloatValue(increaseFloat(complexTypeValue.getFloatValue()));
      complexTypeValue.setDoubleValue(increaseDouble(complexTypeValue.getDoubleValue()));
      complexTypeValue.setStringValue(sayHello(complexTypeValue.getStringValue()));
      Collections.sort(complexTypeValue.getStringList());
      complexTypeValue.setqNameValue(modifyQName(complexTypeValue.getqNameValue()));
      complexTypeValue.setDateValue(getDatePlusDay(complexTypeValue.getDateValue()));
      complexTypeValue.setCalendarValue(getCalendarPlusDay(complexTypeValue.getCalendarValue()));
      return complexTypeValue;
   }
}
