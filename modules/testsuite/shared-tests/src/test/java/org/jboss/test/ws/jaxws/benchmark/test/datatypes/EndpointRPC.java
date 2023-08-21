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

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
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
