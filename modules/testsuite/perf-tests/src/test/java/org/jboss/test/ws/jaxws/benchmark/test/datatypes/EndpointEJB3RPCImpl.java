/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.benchmark.test.datatypes;

import java.math.BigDecimal;
import java.util.Date;

import javax.ejb.Stateless;
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
@WebService(serviceName = "EndpointRPCService", portName = "EndpointRPCPort", endpointInterface = "org.jboss.test.ws.jaxws.benchmark.test.datatypes.EndpointRPC")
@Stateless
public class EndpointEJB3RPCImpl implements EndpointRPC
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
