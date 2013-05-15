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
package org.jboss.test.ws.jaxrpc.samples.holder;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.GregorianCalendar;

import javax.xml.namespace.QName;
import javax.xml.rpc.Call;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceFactory;
import javax.xml.rpc.holders.BigDecimalHolder;
import javax.xml.rpc.holders.BigIntegerHolder;
import javax.xml.rpc.holders.BooleanHolder;
import javax.xml.rpc.holders.BooleanWrapperHolder;
import javax.xml.rpc.holders.ByteArrayHolder;
import javax.xml.rpc.holders.ByteHolder;
import javax.xml.rpc.holders.ByteWrapperHolder;
import javax.xml.rpc.holders.CalendarHolder;
import javax.xml.rpc.holders.DoubleHolder;
import javax.xml.rpc.holders.DoubleWrapperHolder;
import javax.xml.rpc.holders.FloatHolder;
import javax.xml.rpc.holders.FloatWrapperHolder;
import javax.xml.rpc.holders.IntHolder;
import javax.xml.rpc.holders.IntegerWrapperHolder;
import javax.xml.rpc.holders.LongHolder;
import javax.xml.rpc.holders.LongWrapperHolder;
import javax.xml.rpc.holders.QNameHolder;
import javax.xml.rpc.holders.ShortHolder;
import javax.xml.rpc.holders.ShortWrapperHolder;
import javax.xml.rpc.holders.StringHolder;

import junit.framework.Test;

import org.jboss.ws.common.Constants;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test DII with Holders
 *
 * @author Thomas.Diesler@jboss.org
 * @since 22-Dec-2004
 */
public class HolderDIITestCase extends JBossWSTest
{
   private final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/jaxrpc-samples-holder";
   private static final String TARGET_NAMESPACE = "http://org.jboss.ws/samples/holder";

   public static Test suite()
   {
      return new JBossWSTestSetup(HolderDIITestCase.class, "jaxrpc-samples-holder.war");
   }

   public void testEchoBigDecimal() throws Exception
   {
      Service service = ServiceFactory.newInstance().createService(new QName("testService"));

      Call call = service.createCall();
      call.setOperationName(new QName(TARGET_NAMESPACE, "echoBigDecimal"));
      call.addParameter("BigDecimal_1", Constants.TYPE_LITERAL_DECIMAL, ParameterMode.INOUT);
      call.setTargetEndpointAddress(TARGET_ENDPOINT_ADDRESS);

      BigDecimalHolder holder = new BigDecimalHolder(new BigDecimal("1000"));
      Object retObj = call.invoke(new Object[]{holder});
      assertNull(retObj);
      assertEquals(new BigDecimal("1001"), holder.value);
   }

   public void testEchoBigInteger() throws Exception
   {
      Service service = ServiceFactory.newInstance().createService(new QName("testService"));

      Call call = service.createCall();
      call.setOperationName(new QName(TARGET_NAMESPACE, "echoBigInteger"));
      call.addParameter("BigInteger_1", Constants.TYPE_LITERAL_INTEGER, ParameterMode.INOUT);
      call.setTargetEndpointAddress(TARGET_ENDPOINT_ADDRESS);

      BigIntegerHolder holder = new BigIntegerHolder(new BigInteger("1000"));
      Object retObj = call.invoke(new Object[]{holder});
      assertNull(retObj);
      assertEquals(new BigInteger("1001"), holder.value);
   }

   public void testEchoBoolean() throws Exception
   {
      Service service = ServiceFactory.newInstance().createService(new QName("testService"));

      Call call = service.createCall();
      call.setOperationName(new QName(TARGET_NAMESPACE, "echoBoolean"));
      call.addParameter("boolean_1", Constants.TYPE_LITERAL_BOOLEAN, ParameterMode.INOUT);
      call.setTargetEndpointAddress(TARGET_ENDPOINT_ADDRESS);

      BooleanHolder holder = new BooleanHolder(false);
      Object retObj = call.invoke(new Object[]{holder});
      assertNull(retObj);
      assertEquals(true, holder.value);
   }

   public void testEchoBooleanWrapper() throws Exception
   {
      Service service = ServiceFactory.newInstance().createService(new QName("testService"));

      Call call = service.createCall();
      call.setOperationName(new QName(TARGET_NAMESPACE, "echoBooleanWrapper"));
      call.addParameter("Boolean_1", Constants.TYPE_LITERAL_BOOLEAN, ParameterMode.INOUT);
      call.setTargetEndpointAddress(TARGET_ENDPOINT_ADDRESS);

      BooleanWrapperHolder holder = new BooleanWrapperHolder(new Boolean(false));
      Object retObj = call.invoke(new Object[]{holder});
      assertNull(retObj);
      assertEquals(new Boolean(true), holder.value);
   }

   public void testEchoByteArray() throws Exception
   {
      Service service = ServiceFactory.newInstance().createService(new QName("testService"));

      Call call = service.createCall();
      call.setOperationName(new QName(TARGET_NAMESPACE, "echoByteArray"));
      call.addParameter("arrayOfbyte_1", Constants.TYPE_LITERAL_BASE64BINARY, ParameterMode.INOUT);
      call.setTargetEndpointAddress(TARGET_ENDPOINT_ADDRESS);

      ByteArrayHolder holder = new ByteArrayHolder(new String("Some base64 msg").getBytes());
      Object retObj = call.invoke(new Object[]{holder});
      assertNull(retObj);
      assertEquals("Some base64 msgResponse", new String(holder.value));
   }

   public void testEchoByte() throws Exception
   {
      Service service = ServiceFactory.newInstance().createService(new QName("testService"));

      Call call = service.createCall();
      call.setOperationName(new QName(TARGET_NAMESPACE, "echoByte"));
      call.addParameter("byte_1", Constants.TYPE_LITERAL_BYTE, ParameterMode.INOUT);
      call.setTargetEndpointAddress(TARGET_ENDPOINT_ADDRESS);

      ByteHolder holder = new ByteHolder((byte)0x45);
      Object retObj = call.invoke(new Object[]{holder});
      assertNull(retObj);
      assertEquals((byte)0x46, holder.value);
   }

   public void testEchoByteWrapper() throws Exception
   {
      Service service = ServiceFactory.newInstance().createService(new QName("testService"));

      Call call = service.createCall();
      call.setOperationName(new QName(TARGET_NAMESPACE, "echoByteWrapper"));
      call.addParameter("Byte_1", Constants.TYPE_LITERAL_BYTE, ParameterMode.INOUT);
      call.setTargetEndpointAddress(TARGET_ENDPOINT_ADDRESS);

      ByteWrapperHolder holder = new ByteWrapperHolder(new Byte((byte)0x45));
      Object retObj = call.invoke(new Object[]{holder});
      assertNull(retObj);
      assertEquals(new Byte((byte)0x46), holder.value);
   }

   public void testEchoCalendar() throws Exception
   {
      Service service = ServiceFactory.newInstance().createService(new QName("testService"));

      Call call = service.createCall();
      call.setOperationName(new QName(TARGET_NAMESPACE, "echoCalendar"));
      call.addParameter("Calendar_1", Constants.TYPE_LITERAL_DATETIME, ParameterMode.INOUT);
      call.setTargetEndpointAddress(TARGET_ENDPOINT_ADDRESS);

      GregorianCalendar value = new GregorianCalendar(2004, 11, 23, 11, 58, 23);
      CalendarHolder holder = new CalendarHolder(value);
      Object retObj = call.invoke(new Object[]{holder});
      assertNull(retObj);
      assertEquals(value.getTime().getTime(), holder.value.getTime().getTime());
   }

   public void testEchoDouble() throws Exception
   {
      Service service = ServiceFactory.newInstance().createService(new QName("testService"));

      Call call = service.createCall();
      call.setOperationName(new QName(TARGET_NAMESPACE, "echoDouble"));
      call.addParameter("double_1", Constants.TYPE_LITERAL_DOUBLE, ParameterMode.INOUT);
      call.setTargetEndpointAddress(TARGET_ENDPOINT_ADDRESS);

      DoubleHolder holder = new DoubleHolder(1.2);
      Object retObj = call.invoke(new Object[]{holder});
      assertNull(retObj);
      assertEquals(2.2, holder.value, 0.01);
   }

   public void testEchoDoubleWrapper() throws Exception
   {
      Service service = ServiceFactory.newInstance().createService(new QName("testService"));

      Call call = service.createCall();
      call.setOperationName(new QName(TARGET_NAMESPACE, "echoDoubleWrapper"));
      call.addParameter("Double_1", Constants.TYPE_LITERAL_DOUBLE, ParameterMode.INOUT);
      call.setTargetEndpointAddress(TARGET_ENDPOINT_ADDRESS);

      DoubleWrapperHolder holder = new DoubleWrapperHolder(new Double(1.2));
      Object retObj = call.invoke(new Object[]{holder});
      assertNull(retObj);
      assertEquals(2.2, holder.value.doubleValue(), 0.01);
   }

   public void testEchoFloat() throws Exception
   {
      Service service = ServiceFactory.newInstance().createService(new QName("testService"));

      Call call = service.createCall();
      call.setOperationName(new QName(TARGET_NAMESPACE, "echoFloat"));
      call.addParameter("float_1", Constants.TYPE_LITERAL_FLOAT, ParameterMode.INOUT);
      call.setTargetEndpointAddress(TARGET_ENDPOINT_ADDRESS);

      FloatHolder holder = new FloatHolder(1.2f);
      Object retObj = call.invoke(new Object[]{holder});
      assertNull(retObj);
      assertEquals(2.2f, holder.value, 0.01);
   }

   public void testEchoFloatWrapper() throws Exception
   {
      Service service = ServiceFactory.newInstance().createService(new QName("testService"));

      Call call = service.createCall();
      call.setOperationName(new QName(TARGET_NAMESPACE, "echoFloatWrapper"));
      call.addParameter("Float_1", Constants.TYPE_LITERAL_FLOAT, ParameterMode.INOUT);
      call.setTargetEndpointAddress(TARGET_ENDPOINT_ADDRESS);

      FloatWrapperHolder holder = new FloatWrapperHolder(new Float(1.2));
      Object retObj = call.invoke(new Object[]{holder});
      assertNull(retObj);
      assertEquals(2.2, holder.value.floatValue(), 0.01);
   }

   public void testEchoIntegerWrapper() throws Exception
   {
      Service service = ServiceFactory.newInstance().createService(new QName("testService"));

      Call call = service.createCall();
      call.setOperationName(new QName(TARGET_NAMESPACE, "echoIntegerWrapper"));
      call.addParameter("Integer_1", Constants.TYPE_LITERAL_INT, ParameterMode.INOUT);
      call.setTargetEndpointAddress(TARGET_ENDPOINT_ADDRESS);

      IntegerWrapperHolder holder = new IntegerWrapperHolder(new Integer(1));
      Object retObj = call.invoke(new Object[]{holder});
      assertNull(retObj);
      assertEquals(2, holder.value.intValue());
   }

   public void testEchoInt() throws Exception
   {
      Service service = ServiceFactory.newInstance().createService(new QName("testService"));

      Call call = service.createCall();
      call.setOperationName(new QName(TARGET_NAMESPACE, "echoInt"));
      call.addParameter("int_1", Constants.TYPE_LITERAL_INT, ParameterMode.INOUT);
      call.setTargetEndpointAddress(TARGET_ENDPOINT_ADDRESS);

      IntHolder holder = new IntHolder(1);
      Object retObj = call.invoke(new Object[]{holder});
      assertNull(retObj);
      assertEquals(2, holder.value, 0.01);
   }

   public void testEchoLong() throws Exception
   {
      Service service = ServiceFactory.newInstance().createService(new QName("testService"));

      Call call = service.createCall();
      call.setOperationName(new QName(TARGET_NAMESPACE, "echoLong"));
      call.addParameter("long_1", Constants.TYPE_LITERAL_LONG, ParameterMode.INOUT);
      call.setTargetEndpointAddress(TARGET_ENDPOINT_ADDRESS);

      LongHolder holder = new LongHolder(1);
      Object retObj = call.invoke(new Object[]{holder});
      assertNull(retObj);
      assertEquals(2, holder.value);
   }

   public void testEchoLongWrapper() throws Exception
   {
      Service service = ServiceFactory.newInstance().createService(new QName("testService"));

      Call call = service.createCall();
      call.setOperationName(new QName(TARGET_NAMESPACE, "echoLongWrapper"));
      call.addParameter("Long_1", Constants.TYPE_LITERAL_LONG, ParameterMode.INOUT);
      call.setTargetEndpointAddress(TARGET_ENDPOINT_ADDRESS);

      LongWrapperHolder holder = new LongWrapperHolder(new Long(1));
      Object retObj = call.invoke(new Object[]{holder});
      assertNull(retObj);
      assertEquals(2, holder.value.longValue());
   }

   public void testEchoQName() throws Exception
   {
      Service service = ServiceFactory.newInstance().createService(new QName("testService"));

      Call call = service.createCall();
      call.setOperationName(new QName(TARGET_NAMESPACE, "echoQName"));
      call.addParameter("QName_1", Constants.TYPE_LITERAL_QNAME, ParameterMode.INOUT);
      call.setTargetEndpointAddress(TARGET_ENDPOINT_ADDRESS);

      QNameHolder holder = new QNameHolder(new QName("http://somens", "localPart", "ns1"));
      Object retObj = call.invoke(new Object[]{holder});
      assertNull(retObj);
      assertEquals("{http://somens}localPartResponse", holder.value.toString());
   }

   public void testEchoShort() throws Exception
   {
      Service service = ServiceFactory.newInstance().createService(new QName("testService"));

      Call call = service.createCall();
      call.setOperationName(new QName(TARGET_NAMESPACE, "echoShort"));
      call.addParameter("short_1", Constants.TYPE_LITERAL_SHORT, ParameterMode.INOUT);
      call.setTargetEndpointAddress(TARGET_ENDPOINT_ADDRESS);

      ShortHolder holder = new ShortHolder((short)1);
      Object retObj = call.invoke(new Object[]{holder});
      assertNull(retObj);
      assertEquals(2, holder.value);
   }

   public void testEchoShortWrapper() throws Exception
   {
      Service service = ServiceFactory.newInstance().createService(new QName("testService"));

      Call call = service.createCall();
      call.setOperationName(new QName(TARGET_NAMESPACE, "echoShortWrapper"));
      call.addParameter("Short_1", Constants.TYPE_LITERAL_SHORT, ParameterMode.INOUT);
      call.setTargetEndpointAddress(TARGET_ENDPOINT_ADDRESS);

      ShortWrapperHolder holder = new ShortWrapperHolder(new Short((short)1));
      Object retObj = call.invoke(new Object[]{holder});
      assertNull(retObj);
      assertEquals(2, holder.value.shortValue());
   }

   public void testEchoString() throws Exception
   {
      Service service = ServiceFactory.newInstance().createService(new QName("testService"));

      Call call = service.createCall();
      call.setOperationName(new QName(TARGET_NAMESPACE, "echoString"));
      call.addParameter("String_1", Constants.TYPE_LITERAL_STRING, ParameterMode.INOUT);
      call.setTargetEndpointAddress(TARGET_ENDPOINT_ADDRESS);

      StringHolder holder = new StringHolder("Hello world!");
      Object retObj = call.invoke(new Object[]{holder});
      assertNull(retObj);
      assertEquals("Hello world!Response", holder.value);
   }
}
