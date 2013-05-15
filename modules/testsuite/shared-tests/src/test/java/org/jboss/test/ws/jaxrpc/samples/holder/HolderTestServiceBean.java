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
import java.util.Date;

import javax.xml.namespace.QName;
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

import org.jboss.logging.Logger;

/**
 * A service endpoint for the HolderTestCase
 *
 * @author Thomas.Diesler@jboss.org
 * @since 22-Dec-2004
 */
public class HolderTestServiceBean
{
   // Provide logging
   private static Logger log = Logger.getLogger(HolderTestServiceBean.class);

   public void echoBigDecimal(BigDecimalHolder val)
   {
      log.info("echoBigDecimal: " + val.value);
      val.value = val.value.add(new BigDecimal("1"));
   }

   public void echoBigInteger(BigIntegerHolder val)
   {
      log.info("echoBigInteger: " + val.value);
      val.value = val.value.add(new BigInteger("1"));
   }

   public void echoBoolean(BooleanHolder val)
   {
      log.info("echoBoolean: " + val.value);
      val.value = !val.value;
   }

   public void echoBooleanWrapper(BooleanWrapperHolder val)
   {
      log.info("echoBooleanWrapper: " + val.value);
      val.value = new Boolean(!val.value.booleanValue());
   }

   public void echoByteArray(ByteArrayHolder val)
   {
      log.info("echoByteArray: " + new String(val.value));
      val.value = new String(new String(val.value) + "Response").getBytes();
   }

   public void echoByte(ByteHolder val)
   {
      log.info("echoByte: " + val.value);
      val.value = (byte)(val.value + 1);
   }

   public void echoByteWrapper(ByteWrapperHolder val)
   {
      log.info("echoByteWrapper: " + val.value);
      val.value = new Byte((byte)(val.value.byteValue() + 1));
   }

   public void echoCalendar(CalendarHolder val)
   {
      log.info("echoCalendar: " + val.value.getTime());
      val.value.setTime(new Date(val.value.getTime().getTime()));
   }

   public void echoDouble(DoubleHolder val)
   {
      log.info("echoDouble: " + val.value);
      val.value = val.value + 1;
   }

   public void echoDoubleWrapper(DoubleWrapperHolder val)
   {
      log.info("echoDoubleWrapper: " + val.value);
      val.value = new Double(val.value.doubleValue() + 1);
   }

   public void echoFloat(FloatHolder val)
   {
      log.info("echoFloat: " + val.value);
      val.value = val.value + 1;
   }

   public void echoFloatWrapper(FloatWrapperHolder val)
   {
      log.info("echoFloatWrapper: " + val.value);
      val.value = new Float(val.value.floatValue() + 1);
   }

   public void echoIntegerWrapper(IntegerWrapperHolder val)
   {
      log.info("echoIntegerWrapper: " + val.value);
      val.value = new Integer(val.value.intValue() + 1);
   }

   public void echoInt(IntHolder val)
   {
      log.info("echoInt: " + val.value);
      val.value = val.value + 1;
   }

   public void echoLong(LongHolder val)
   {
      log.info("echoLong: " + val.value);
      val.value = val.value + 1;
   }

   public void echoLongWrapper(LongWrapperHolder val)
   {
      log.info("echoLongWrapper: " + val.value);
      val.value = new Long(val.value.longValue() + 1);
   }

   /*
   public void echoObject(ObjectHolder val)
   {
      log.info("echoObject: " + val.value);
   }
   */

   public void echoQName(QNameHolder val)
   {
      log.info("echoQName: " + val.value);
      QName qn = val.value;
      val.value = new QName(qn.getNamespaceURI(), qn.getLocalPart() + "Response", qn.getPrefix());
   }

   public void echoShort(ShortHolder val)
   {
      log.info("echoShort: " + val.value);
      val.value = (short)(val.value + 1);
   }

   public void echoShortWrapper(ShortWrapperHolder val)
   {
      log.info("echoShortWrapper: " + val.value);
      val.value = new Short((short)(val.value.shortValue() + 1));
   }

   public void echoString(StringHolder val)
   {
      log.info("echoString: " + val.value);
      val.value = val.value + "Response";
   }
}
