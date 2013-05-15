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
import java.net.URL;
import java.util.GregorianCalendar;

import javax.naming.InitialContext;
import javax.xml.namespace.QName;
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

import org.jboss.wsf.test.CleanupOperation;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test Holders
 *
 * @author Thomas.Diesler@jboss.org
 * @since 22-Dec-2004
 */
public class HolderTestCase extends JBossWSTest
{
   private static final String TARGET_ENDPOINT_URL = "http://" + getServerHost() + ":8080/jaxrpc-samples-holder";
   private static final String TARGET_NAMESPACE = "http://org.jboss.ws/samples/holder";
   
   private static HolderTestService port;

   public static Test suite()
   {
      return new JBossWSTestSetup(HolderDIITestCase.class, "jaxrpc-samples-holder.war, jaxrpc-samples-holder-client.jar", new CleanupOperation() {
         @Override
         public void cleanUp() {
            port = null;
         }
      });
   }

   protected void setUp() throws Exception
   {
      super.setUp();

      if (port == null)
      {
         port = getService(HolderTestService.class, "TestService", "HolderTestServicePort");
      }
   }
   
   protected <T> T getService(final Class<T> clazz, final String serviceName, final String portName) throws Exception {
      ServiceFactory serviceFactory = ServiceFactory.newInstance();
      Service service = serviceFactory.createService(new URL(TARGET_ENDPOINT_URL + "?wsdl"), new QName(TARGET_NAMESPACE, serviceName));
      return (T) service.getPort(new QName(TARGET_NAMESPACE, portName), clazz);
   }

   public void testEchoBigDecimal() throws Exception
   {
      BigDecimalHolder holder = new BigDecimalHolder(new BigDecimal("1000"));
      port.echoBigDecimal(holder);
      assertEquals(new BigDecimal("1001"), holder.value);
   }

   public void testEchoBigInteger() throws Exception
   {
      BigIntegerHolder holder = new BigIntegerHolder(new BigInteger("1000"));
      port.echoBigInteger(holder);
      assertEquals(new BigInteger("1001"), holder.value);
   }

   public void testEchoBoolean() throws Exception
   {
      BooleanHolder holder = new BooleanHolder(false);
      port.echoBoolean(holder);
      assertEquals(true, holder.value);
   }

   public void testEchoBooleanWrapper() throws Exception
   {
      BooleanWrapperHolder holder = new BooleanWrapperHolder(new Boolean(false));
      port.echoBooleanWrapper(holder);
      assertEquals(new Boolean(true), holder.value);
   }

   public void testEchoByteArray() throws Exception
   {
      ByteArrayHolder holder = new ByteArrayHolder(new String("Some base64 msg").getBytes());
      port.echoByteArray(holder);
      assertEquals("Some base64 msgResponse", new String(holder.value));
   }

   public void testEchoByte() throws Exception
   {
      ByteHolder holder = new ByteHolder((byte)0x45);
      port.echoByte(holder);
      assertEquals((byte)0x46, holder.value);
   }

   public void testEchoByteWrapper() throws Exception
   {
      ByteWrapperHolder holder = new ByteWrapperHolder(new Byte((byte)0x45));
      port.echoByteWrapper(holder);
      assertEquals(new Byte((byte)0x46), holder.value);
   }

   public void testEchoCalendar() throws Exception
   {
      GregorianCalendar value = new GregorianCalendar(2004, 11, 23, 11, 58, 23);
      CalendarHolder holder = new CalendarHolder(value);
      port.echoCalendar(holder);
      assertEquals(value.getTime().getTime(), holder.value.getTime().getTime());
   }

   public void testEchoDouble() throws Exception
   {
      DoubleHolder holder = new DoubleHolder(1.2);
      port.echoDouble(holder);
      assertEquals(2.2, holder.value, 0.01);
   }

   public void testEchoDoubleWrapper() throws Exception
   {
      DoubleWrapperHolder holder = new DoubleWrapperHolder(new Double(1.2));
      port.echoDoubleWrapper(holder);
      assertEquals(2.2, holder.value.doubleValue(), 0.01);
   }

   public void testEchoFloat() throws Exception
   {
      FloatHolder holder = new FloatHolder(1.2f);
      port.echoFloat(holder);
      assertEquals(2.2f, holder.value, 0.01);
   }

   public void testEchoFloatWrapper() throws Exception
   {
      FloatWrapperHolder holder = new FloatWrapperHolder(new Float(1.2));
      port.echoFloatWrapper(holder);
      assertEquals(2.2, holder.value.floatValue(), 0.01);
   }

   public void testEchoIntegerWrapper() throws Exception
   {
      IntegerWrapperHolder holder = new IntegerWrapperHolder(new Integer(1));
      port.echoIntegerWrapper(holder);
      assertEquals(2, holder.value.intValue());
   }

   public void testEchoInt() throws Exception
   {
      IntHolder holder = new IntHolder(1);
      port.echoInt(holder);
      assertEquals(2, holder.value, 0.01);
   }

   public void testEchoLong() throws Exception
   {
      LongHolder holder = new LongHolder(1);
      port.echoLong(holder);
      assertEquals(2, holder.value);
   }

   public void testEchoLongWrapper() throws Exception
   {
      LongWrapperHolder holder = new LongWrapperHolder(new Long(1));
      port.echoLongWrapper(holder);
      assertEquals(2, holder.value.longValue());
   }

   public void testEchoQName() throws Exception
   {
      QNameHolder holder = new QNameHolder(new QName("http://somens", "localPart", "ns1"));
      port.echoQName(holder);
      assertEquals("{http://somens}localPartResponse", holder.value.toString());
   }

   public void testEchoShort() throws Exception
   {
      ShortHolder holder = new ShortHolder((short)1);
      port.echoShort(holder);
      assertEquals(2, holder.value);
   }

   public void testEchoShortWrapper() throws Exception
   {
      ShortWrapperHolder holder = new ShortWrapperHolder(new Short((short)1));
      port.echoShortWrapper(holder);
      assertEquals(2, holder.value.shortValue());
   }

   public void testEchoString() throws Exception
   {
      StringHolder holder = new StringHolder("Hello world!");
      port.echoString(holder);
      assertEquals("Hello world!Response", holder.value);
   }
}
