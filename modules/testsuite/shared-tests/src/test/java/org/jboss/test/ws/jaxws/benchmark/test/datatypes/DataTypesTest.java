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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.jboss.test.ws.jaxws.benchmark.BenchmarkTest;
import org.jboss.test.ws.jaxws.benchmark.test.datatypes.types.ComplexType;

public abstract class DataTypesTest implements BenchmarkTest
{

   protected static final XMLGregorianCalendar testedCalendar;

   protected static final XMLGregorianCalendar expectedCalendar;

   protected static final String testedString = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<Order orderId=\"1\" orderDate=\"Wed Nov 15 13:45:28 EST 2006\"\n statusCode=\"9\" netAmount=\"59.97\" totalAmount=\"64.92\" tax=\"4.95\">\n  <Customer userName=\"user1\" firstName=\"Harry\" lastName=\"Fletcher\"\n        state=\"SD\" />\n   <OrderLines>\n      <OrderLine position=\"1\" quantity=\"1\">\n         <Product productId=\"364\" title=\"The 40-Year-Old Virgin \"\n              price=\"29.98\" />\n        </OrderLine>\n      <OrderLine position=\"2\" quantity=\"1\">\n         <Product productId=\"299\" title=\"Pulp Fiction\" price=\"29.99\" />\n      </OrderLine>\n      <OrderLine position=\"3\" quantity=\"1\">\n         <Product productId=\"364\" title=\"The 40-Year-Old Virgin \"\n              price=\"29.98\" />\n        </OrderLine>\n      <OrderLine position=\"4\" quantity=\"1\">\n         <Product productId=\"299\" title=\"Pulp Fiction\" price=\"29.99\" />\n      </OrderLine>\n      <OrderLine position=\"5\" quantity=\"1\">\n         <Product productId=\"364\" title=\"The 40-Year-Old Virgin \"\n              price=\"29.98\" />\n        </OrderLine>\n      <OrderLine position=\"6\" quantity=\"1\">\n         <Product productId=\"299\" title=\"Pulp Fiction\" price=\"29.99\" />\n      </OrderLine>\n      <OrderLine position=\"7\" quantity=\"1\">\n         <Product productId=\"364\" title=\"The 40-Year-Old Virgin \"\n              price=\"29.98\" />\n        </OrderLine>\n      <OrderLine position=\"8\" quantity=\"1\">\n         <Product productId=\"299\" title=\"Pulp Fiction\" price=\"29.99\" />\n      </OrderLine>\n      <OrderLine position=\"9\" quantity=\"1\">\n         <Product productId=\"364\" title=\"The 40-Year-Old Virgin \"\n              price=\"29.98\" />\n        </OrderLine>\n      <OrderLine position=\"10\" quantity=\"1\">\n            <Product productId=\"299\" title=\"Pulp Fiction\" price=\"29.99\" />\n      </OrderLine>\n      <OrderLine position=\"11\" quantity=\"1\">\n            <Product productId=\"364\" title=\"The 40-Year-Old Virgin \"\n              price=\"29.98\" />\n        </OrderLine>\n      <OrderLine position=\"12\" quantity=\"1\">\n            <Product productId=\"299\" title=\"Pulp Fiction\" price=\"29.99\" />\n      </OrderLine>\n      <OrderLine position=\"13\" quantity=\"1\">\n            <Product productId=\"364\" title=\"The 40-Year-Old Virgin \"\n              price=\"29.98\" />\n        </OrderLine>\n      <OrderLine position=\"14\" quantity=\"1\">\n            <Product productId=\"299\" title=\"Pulp Fiction\" price=\"29.99\" />\n      </OrderLine>\n      <OrderLine position=\"15\" quantity=\"1\">\n            <Product productId=\"364\" title=\"The 40-Year-Old Virgin \"\n              price=\"29.98\" />\n        </OrderLine>\n      <OrderLine position=\"16\" quantity=\"1\">\n            <Product productId=\"299\" title=\"Pulp Fiction\" price=\"29.99\" />\n      </OrderLine>\n      <OrderLine position=\"17\" quantity=\"1\">\n            <Product productId=\"364\" title=\"The 40-Year-Old Virgin \"\n              price=\"29.98\" />\n        </OrderLine>\n      <OrderLine position=\"18\" quantity=\"1\">\n            <Product productId=\"299\" title=\"Pulp Fiction\" price=\"29.99\" />\n      </OrderLine>\n      <OrderLine position=\"19\" quantity=\"1\">\n            <Product productId=\"364\" title=\"The 40-Year-Old Virgin \"\n              price=\"29.98\" />\n        </OrderLine>\n      <OrderLine position=\"20\" quantity=\"1\">\n            <Product productId=\"299\" title=\"Pulp Fiction\" price=\"29.99\" />\n      </OrderLine>\n      <OrderLine position=\"21\" quantity=\"1\">\n            <Product productId=\"364\" title=\"The 40-Year-Old Virgin \"\n              price=\"29.98\" />\n        </OrderLine>\n      <OrderLine position=\"22\" quantity=\"1\">\n            <Product productId=\"299\" title=\"Pulp Fiction\" price=\"29.99\" />\n      </OrderLine>\n      <OrderLine position=\"23\" quantity=\"1\">\n            <Product productId=\"364\" title=\"The 40-Year-Old Virgin \"\n              price=\"29.98\" />\n        </OrderLine>\n      <OrderLine position=\"24\" quantity=\"1\">\n            <Product productId=\"299\" title=\"Pulp Fiction\" price=\"29.99\" />\n      </OrderLine>\n      <OrderLine position=\"25\" quantity=\"1\">\n            <Product productId=\"364\" title=\"The 40-Year-Old Virgin \"\n              price=\"29.98\" />\n        </OrderLine>\n      <OrderLine position=\"26\" quantity=\"1\">\n            <Product productId=\"299\" title=\"Pulp Fiction\" price=\"29.99\" />\n      </OrderLine>\n      <OrderLine position=\"27\" quantity=\"1\">\n            <Product productId=\"364\" title=\"The 40-Year-Old Virgin \"\n              price=\"29.98\" />\n        </OrderLine>\n      <OrderLine position=\"28\" quantity=\"1\">\n            <Product productId=\"299\" title=\"Pulp Fiction\" price=\"29.99\" />\n      </OrderLine>\n      <OrderLine position=\"29\" quantity=\"1\">\n            <Product productId=\"364\" title=\"The 40-Year-Old Virgin \"\n              price=\"29.98\" />\n        </OrderLine>\n      <OrderLine position=\"30\" quantity=\"1\">\n            <Product productId=\"299\" title=\"Pulp Fiction\" price=\"29.99\" />\n      </OrderLine>\n      <OrderLine position=\"31\" quantity=\"1\">\n            <Product productId=\"364\" title=\"The 40-Year-Old Virgin \"\n              price=\"29.98\" />\n        </OrderLine>\n      <OrderLine position=\"32\" quantity=\"1\">\n            <Product productId=\"299\" title=\"Pulp Fiction\" price=\"29.99\" />\n      </OrderLine>\n      <OrderLine position=\"33\" quantity=\"1\">\n            <Product productId=\"364\" title=\"The 40-Year-Old Virgin \"\n              price=\"29.98\" />\n        </OrderLine>\n      <OrderLine position=\"34\" quantity=\"1\">\n            <Product productId=\"299\" title=\"Pulp Fiction\" price=\"29.99\" />\n      </OrderLine>\n      <OrderLine position=\"35\" quantity=\"1\">\n            <Product productId=\"364\" title=\"The 40-Year-Old Virgin \"\n              price=\"29.98\" />\n        </OrderLine>\n      <Memo>\n            abcdefghijklmnopqrstuvwxyz0123456789!@#$%^°*()_+-=[]{}:\",.?/`~Pada,pada_jahoda,kterou_sěščřžýáíégggaabcdefghijklmnopqrstuvwxyz0123456789!@#$%^°*()_+-=[]{}:\",.?/`~Pahoda,kterou_sni_pan_Lahodaabcdefghijklmnopqrstuvwxyz0123456789!@#$%^°*()_+-=[]{}:\",.?/`~Pada,pada_jahoda,kterou_sni_pan_Lahoda\n     </Memo>\n   </OrderLines>\n</Order>\n";

   protected static final String expectedString = "Hello World Greeting for '" + testedString + "' today";

   protected static final boolean testedBoolean = true;

   protected static final boolean expectedBoolean = !testedBoolean;

   protected static final byte testedByte = (byte) 63;

   protected static final byte expectedByte = testedByte + 1;

   protected static final byte[] testedByteArray = "!@#$%^°*()_+-=[]{}:\",.?/`~Pada,pada_jahoda,kterou_sěščřžýáíégggaabcdefghijklmnopqrstuvwxyz0123456789"
         .getBytes();

   protected static final byte[] expectedByteArray;

   protected static final short testedShort = (short) 25;

   protected static final short expectedShort = testedShort + 1;

   protected static final int testedInt = 4567;

   protected static final int expectedInt = testedInt + 1;

   protected static final long testedLong = 1234567890L;

   protected static final long expectedLong = testedLong + 1L;

   protected static final float testedFloat = 6.7f;

   protected static final float expectedFloat = testedFloat + 1.0f;

   protected static final double testedDouble = 8.9;

   protected static final double expectedDouble = testedDouble + 1.0f;

   protected static final QName testedQName = new QName("qname_value");

   protected static final QName expectedQName = new QName("qname_value_modified");

   protected static final ComplexType testedComplexType, expectedComplexType, expectedReturnedComplexType;

   protected static final String expectedValuesString;

   protected static final List<String> testedStringList;

   protected static final List<String> expectedStringList;

   protected static final Date testedDate = new Date(1234567890L);

   protected static final Date expectedDate = new Date(testedDate.getTime() + 86400000L);

   static
   {
      DatatypeFactory dtFactory = null;
      GregorianCalendar calendar = new GregorianCalendar();
      Duration dayDuration = null;
      try
      {
         dtFactory = DatatypeFactory.newInstance();
      }
      catch (DatatypeConfigurationException e)
      {

         e.printStackTrace();
      }
      testedCalendar = dtFactory.newXMLGregorianCalendar(calendar);

      dayDuration = dtFactory.newDuration(86400000L);
      expectedCalendar = (XMLGregorianCalendar) testedCalendar.clone();
      expectedCalendar.add(dayDuration);

      testedStringList = new ArrayList<String>();
      for (int i = 0; i < 10; i++)
      {
         testedStringList.add("item " + Math.random());
      }
      expectedStringList = new ArrayList<String>(testedStringList);
      Collections.sort(expectedStringList);

      expectedByteArray = new byte[testedByteArray.length];
      int len = testedByteArray.length;
      for (int i = 0; i < len; i++)
      {
         expectedByteArray[i] = testedByteArray[len - 1 - i];
      }

      StringBuffer sb = new StringBuffer();
      sb.append("[byteValue=" + testedByte);
      sb.append(" byteArrayValue=" + new String(testedByteArray));
      sb.append(" shortValue=" + testedShort);
      sb.append(" intValue=" + testedInt);
      sb.append(" longValue=" + testedLong);
      sb.append(" floatValue=" + testedFloat);
      sb.append(" doubleValue=" + testedDouble);
      sb.append(" stringValue=" + testedString);
      sb.append(" qNameValue=" + testedQName);
      sb.append(" dateValue=" + testedDate);
      sb.append(" calendarValue=" + testedCalendar);
      sb.append("]");
      expectedValuesString = sb.toString();

      List<String> valuesStringList = new ArrayList<String>(11);
      valuesStringList.add(String.valueOf(testedByte));
      valuesStringList.add(Arrays.toString(testedByteArray));
      valuesStringList.add(String.valueOf(testedShort));
      valuesStringList.add(String.valueOf(testedInt));
      valuesStringList.add(String.valueOf(testedLong));
      valuesStringList.add(String.valueOf(testedFloat));
      valuesStringList.add(String.valueOf(testedDouble));
      valuesStringList.add(testedString);
      valuesStringList.add(testedQName.toString());
      valuesStringList.add(testedDate.toString());
      valuesStringList.add(testedCalendar.toString());

      expectedReturnedComplexType = new ComplexType(testedByte, testedByteArray, testedShort, testedInt, testedLong,
            testedFloat, testedDouble, testedString, valuesStringList, testedQName, testedDate, testedCalendar);

      testedComplexType = new ComplexType(testedByte, testedByteArray, testedShort, testedInt, testedLong, testedFloat,
            testedDouble, testedString, testedStringList, testedQName, testedDate, testedCalendar);

      expectedComplexType = new ComplexType(expectedByte, expectedByteArray, expectedShort, expectedInt, expectedLong,
            expectedFloat, expectedDouble, expectedString, expectedStringList, expectedQName, expectedDate,
            expectedCalendar);
   }
}
