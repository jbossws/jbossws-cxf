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
import java.util.Date;

import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

/**
 * @author pmacik@redhat.com
 * @since 09-Mar-2010
 */
@WebService(serviceName = "EndpointDocService", portName = "EndpointDocPort", endpointInterface = "org.jboss.test.ws.jaxws.benchmark.test.datatypes.EndpointDoc")
public class EndpointPOJODocImpl implements EndpointDoc
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

   public String sayHello(@WebParam(name = "toWhom") String toWhom)
   {
      String greeting = "Hello World Greeting for '" + toWhom + "' today";
      return greeting;
   }

   public BigDecimal increaseBigDecimal(@WebParam(name = "value") BigDecimal value)
   {
      return value.add(new BigDecimal(1));
   }

   public boolean negateBoolean(@WebParam(name = "value") boolean value)
   {
      return !value;
   }

   public byte increaseByte(@WebParam(name = "value") byte value)
   {
      return (byte) (value + 1);
   }

   public byte[] reverseByteOrder(@WebParam(name = "data") byte[] data)
   {
      byte[] retVal = new byte[data.length];
      for (int i = 0; i < data.length; i++)
      {
         retVal[i] = data[data.length - 1 - i];
      }
      return retVal;
   }

   public XMLGregorianCalendar getCalendarPlusDay(@WebParam(name = "calendar") XMLGregorianCalendar calendar)
   {
      calendar.add(dayDuration);
      return (XMLGregorianCalendar) calendar.clone();
   }

   public Date getDatePlusDay(@WebParam(name = "date") Date date)
   {
      return new Date(date.getTime() + 86400000L);
   }

   public double increaseDouble(@WebParam(name = "value") double value)
   {
      return value + 1;
   }

   public float increaseFloat(@WebParam(name = "value") float value)
   {
      return value + 1;
   }

   public int increaseInt(@WebParam(name = "value") int value)
   {
      return value + 1;
   }

   public long increaseLong(@WebParam(name = "value") long value)
   {
      return value + 1L;
   }

   public QName modifyQName(@WebParam(name = "value") QName value)
   {
      String modString = "_modified";
      String prefix = value.getPrefix();
      String uri = value.getNamespaceURI();
      String localPart = value.getLocalPart() + modString;
      QName retVal = new QName(uri, localPart, prefix);
      return retVal;
   }

   public short increaseShort(@WebParam(name = "value") short value)
   {
      return (short) (value + 1);
   }
}
