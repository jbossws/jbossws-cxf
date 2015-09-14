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

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

/**
 * @author pmacik@redhat.com
 * @since 09-Mar-2010
 */
@WebService(name = "EndpointRPC", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface EndpointRPC
{

   @WebMethod(operationName = "sayHello", action = "urn:SayHello")
   public String sayHello(String toWhom);

   @WebMethod(operationName = "increaseBigDecimal", action = "urn:IncreaseBigDecimal")
   public BigDecimal increaseBigDecimal(BigDecimal value);

   @WebMethod(operationName = "negateBoolean", action = "urn:NegateBoolean")
   public boolean negateBoolean(boolean value);

   @WebMethod(operationName = "increaseByte", action = "urn:IncreaseByte")
   public byte increaseByte(byte value);

   @WebMethod(operationName = "reverseByteOrder", action = "urn:ReverseByteOrder")
   public byte[] reverseByteOrder(byte[] data);

   @WebMethod(operationName = "getCalendarPlusDay", action = "urn:GetCalendarPlusDay")
   public XMLGregorianCalendar getCalendarPlusDay(XMLGregorianCalendar calendar);

   @WebMethod(operationName = "getDatePlusDay", action = "urn:GetDatePlusDay")
   public Date getDatePlusDay(Date date);

   @WebMethod(operationName = "increaseDouble", action = "urn:IncreaseDouble")
   public double increaseDouble(double value);

   @WebMethod(operationName = "increaseFloat", action = "urn:IncreaseFloat")
   public float increaseFloat(float value);

   @WebMethod(operationName = "increaseInt", action = "urn:IncreaseInt")
   public int increaseInt(int value);

   @WebMethod(operationName = "increaseLong", action = "urn:IncreaseLong")
   public long increaseLong(long value);

   @WebMethod(operationName = "modifyQName", action = "urn:ModifyQName")
   public QName modifyQName(QName value);

   @WebMethod(operationName = "increaseShort", action = "urn:IncreaseShort")
   public short increaseShort(short value);
}
