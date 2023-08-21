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
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import jakarta.xml.ws.RequestWrapper;
import jakarta.xml.ws.ResponseWrapper;

import org.jboss.test.ws.jaxws.benchmark.test.datatypes.types.ComplexType;

/**
 * @author pmacik@redhat.com
 * @since 09-Mar-2010
 */
@WebService(name = "EndpointWrappedRPC", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface EndpointWrappedRPC
{

   @WebMethod(operationName = "sayHello", action = "urn:SayHello")
   @WebResult(name = "Response", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/")
   @RequestWrapper(localName = "SayHello", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.benchmark.test.datatypes.wrappers.SayHello")
   @ResponseWrapper(localName = "SayHelloResponse", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.benchmark.test.datatypes.wrappers.SayHelloResponse")
   public String sayHello(
         @WebParam(name = "ToWhom", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/") String toWhom);

   @WebMethod(operationName = "increaseBigDecimal", action = "urn:IncreaseBigDecimal")
   @WebResult(name = "Response", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/")
   @RequestWrapper(localName = "IncreaseBigDecimal", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.benchmark.test.datatypes.wrappers.IncreaseBigDecimal")
   @ResponseWrapper(localName = "IncreaseBigDecimalResponse", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.benchmark.test.datatypes.wrappers.IncreaseBigDecimalResponse")
   public BigDecimal increaseBigDecimal(
         @WebParam(name = "Value", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/") BigDecimal value);

   @WebMethod(operationName = "negateBoolean", action = "urn:NegateBoolean")
   @WebResult(name = "Response", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/")
   @RequestWrapper(localName = "NegateBoolean", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.benchmark.test.datatypes.wrappers.NegateBoolean")
   @ResponseWrapper(localName = "NegateBooleanResponse", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.benchmark.test.datatypes.wrappers.NegateBooleanResponse")
   public boolean negateBoolean(
         @WebParam(name = "Value", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/") boolean value);

   @WebMethod(operationName = "increaseByte", action = "urn:IncreaseByte")
   @WebResult(name = "Response", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/")
   @RequestWrapper(localName = "IncreaseByte", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.benchmark.test.datatypes.wrappers.IncreaseByte")
   @ResponseWrapper(localName = "IncreaseByteResponse", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.benchmark.test.datatypes.wrappers.IncreaseByteResponse")
   public byte increaseByte(
         @WebParam(name = "Value", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/") byte value);

   @WebMethod(operationName = "reverseByteOrder", action = "urn:ReverseByteOrder")
   @WebResult(name = "Response", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/")
   @RequestWrapper(localName = "ReverseByteOrder", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.benchmark.test.datatypes.wrappers.ReverseByteOrder")
   @ResponseWrapper(localName = "ReverseByteOrderResponse", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.benchmark.test.datatypes.wrappers.ReverseByteOrderResponse")
   public byte[] reverseByteOrder(
         @WebParam(name = "Data", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/") byte[] data);

   @WebMethod(operationName = "getCalendarPlusDay", action = "urn:GetCalendarPlusDay")
   @WebResult(name = "Response", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/")
   @RequestWrapper(localName = "GetCalendarPlusDay", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.benchmark.test.datatypes.wrappers.GetCalendarPlusDay")
   @ResponseWrapper(localName = "GetCalendarPlusDayResponse", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.benchmark.test.datatypes.wrappers.GetCalendarPlusDayResponse")
   public XMLGregorianCalendar getCalendarPlusDay(
         @WebParam(name = "Calendar", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/") XMLGregorianCalendar calendar);

   @WebMethod(operationName = "getDatePlusDay", action = "urn:GetDatePlusDay")
   @WebResult(name = "Response", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/")
   @RequestWrapper(localName = "GetDatePlusDay", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.benchmark.test.datatypes.wrappers.GetDatePlusDay")
   @ResponseWrapper(localName = "GetDatePlusDayResponse", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.benchmark.test.datatypes.wrappers.GetDatePlusDayResponse")
   public Date getDatePlusDay(
         @WebParam(name = "Date", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/") Date date);

   @WebMethod(operationName = "increaseDouble", action = "urn:IncreaseDouble")
   @WebResult(name = "Response", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/")
   @RequestWrapper(localName = "IncreaseDouble", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.benchmark.test.datatypes.wrappers.IncreaseDouble")
   @ResponseWrapper(localName = "IncreaseDoubleResponse", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.benchmark.test.datatypes.wrappers.IncreaseDoubleResponse")
   public double increaseDouble(
         @WebParam(name = "Value", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/") double value);

   @WebMethod(operationName = "increaseFloat", action = "urn:IncreaseFloat")
   @WebResult(name = "Response", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/")
   @RequestWrapper(localName = "IncreaseFloat", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.benchmark.test.datatypes.wrappers.IncreaseFloat")
   @ResponseWrapper(localName = "IncreaseFloatResponse", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.benchmark.test.datatypes.wrappers.IncreaseFloatResponse")
   public float increaseFloat(
         @WebParam(name = "Value", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/") float value);

   @WebMethod(operationName = "increaseInt", action = "urn:IncreaseInt")
   @WebResult(name = "Response", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/")
   @RequestWrapper(localName = "IncreaseInt", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.benchmark.test.datatypes.wrappers.IncreaseInt")
   @ResponseWrapper(localName = "IncreaseIntResponse", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.benchmark.test.datatypes.wrappers.IncreaseIntResponse")
   public int increaseInt(
         @WebParam(name = "Value", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/") int value);

   @WebMethod(operationName = "increaseLong", action = "urn:IncreaseLong")
   @WebResult(name = "Response", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/")
   @RequestWrapper(localName = "IncreaseLong", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.benchmark.test.datatypes.wrappers.IncreaseLong")
   @ResponseWrapper(localName = "IncreaseLongResponse", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.benchmark.test.datatypes.wrappers.IncreaseLongResponse")
   public long increaseLong(
         @WebParam(name = "Value", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/") long value);

   @WebMethod(operationName = "modifyQName", action = "urn:ModifyQName")
   @WebResult(name = "Response", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/")
   @RequestWrapper(localName = "ModifyQName", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.benchmark.test.datatypes.wrappers.ModifyQName")
   @ResponseWrapper(localName = "ModifyQNameResponse", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.benchmark.test.datatypes.wrappers.ModifyQNameResponse")
   public QName modifyQName(
         @WebParam(name = "Value", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/") QName value);

   @WebMethod(operationName = "increaseShort", action = "urn:IncreaseShort")
   @WebResult(name = "Response", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/")
   @RequestWrapper(localName = "IncreaseShort", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.benchmark.test.datatypes.wrappers.IncreaseShort")
   @ResponseWrapper(localName = "IncreaseShortResponse", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.benchmark.test.datatypes.wrappers.IncreaseShortResponse")
   public short increaseShort(
         @WebParam(name = "Value", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/") short value);

   @WebMethod(operationName = "valuesToString", action = "urn:ValuesToString")
   @WebResult(name = "Response", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/")
   @RequestWrapper(localName = "ValuesToString", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.benchmark.test.datatypes.wrappers.ValuesToString")
   @ResponseWrapper(localName = "ValuesToStringResponse", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.benchmark.test.datatypes.wrappers.ValuesToStringResponse")
   public String valuesToString(
         @WebParam(name = "ByteValue", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/") byte byteValue,
         @WebParam(name = "ByteArrayValue", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/") byte[] byteArrayValue,
         @WebParam(name = "ShortValue", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/") short shortValue,
         @WebParam(name = "IntValue", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/") int intValue,
         @WebParam(name = "LongValue", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/") long longValue,
         @WebParam(name = "FloatValue", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/") float floatValue,
         @WebParam(name = "DoubleValue", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/") double doubleValue,
         @WebParam(name = "StringValue", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/") String stringValue,
         @WebParam(name = "QNameValue", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/") QName qNameValue,
         @WebParam(name = "DateValue", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/") Date dateValue,
         @WebParam(name = "CalendarValue", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/") XMLGregorianCalendar calendarValue);

   @WebMethod(operationName = "valuesToComplexType", action = "urn:ValuesToComplexType")
   @WebResult(name = "Response", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/")
   @RequestWrapper(localName = "ValuesToComplexType", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.benchmark.test.datatypes.wrappers.ValuesToComplexType")
   @ResponseWrapper(localName = "ValuesToComplexTypeResponse", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.benchmark.test.datatypes.wrappers.ValuesToComplexTypeResponse")
   public ComplexType valuesToComplexType(
         @WebParam(name = "ByteValue", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/") byte byteValue,
         @WebParam(name = "ByteArrayValue", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/") byte[] byteArrayValue,
         @WebParam(name = "ShortValue", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/") short shortValue,
         @WebParam(name = "IntValue", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/") int intValue,
         @WebParam(name = "LongValue", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/") long longValue,
         @WebParam(name = "FloatValue", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/") float floatValue,
         @WebParam(name = "DoubleValue", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/") double doubleValue,
         @WebParam(name = "StringValue", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/") String stringValue,
         @WebParam(name = "QNameValue", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/") QName qNameValue,
         @WebParam(name = "DateValue", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/") Date dateValue,
         @WebParam(name = "CalendarValue", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/") XMLGregorianCalendar calendarValue);

   @WebMethod(operationName = "modifyComplexType", action = "urn:ModifyComplexType")
   @WebResult(name = "Response", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/")
   @RequestWrapper(localName = "ModifyComplexType", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.benchmark.test.datatypes.wrappers.ModifyComplexType")
   @ResponseWrapper(localName = "ModifyComplexTypeResponse", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/", className = "org.jboss.test.ws.jaxws.benchmark.test.datatypes.wrappers.ModifyComplexTypeResponse")
   public ComplexType modifyComplexType(
         @WebParam(name = "ComplexTypeValue", targetNamespace = "http://datatypes.test.benchmark.jaxws.ws.test.jboss.org/") ComplexType complexTypeValue);
}
