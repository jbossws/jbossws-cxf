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
package org.jboss.wsf.stack.cxf.client.configuration;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * A test case for the MapToBeanConverter
 * 
 * @author alessio.soldano@jboss.com
 * @since 11-Mar-2015
 * 
 */
public class MapToBeanConverterTest
{
   private Map<String, String> getTestMap() {
      Map<String, String> map = new HashMap<String, String>();
      map.put("bean1", "invalid");
      map.put("##bean1", "org.jboss.wsf.stack.cxf.client.configuration.BeanA");
      map.put("##bean1.name", "Bean1");
      map.put("##bean1.num", "1");
      map.put("##bean1.bigNumber", "1000000000");
      map.put("##bean1.beanB", "##bean2");
      map.put("##bean2", "org.jboss.wsf.stack.cxf.client.configuration.BeanB");
      map.put("##bean2.name", "Bean2");
      map.put("##bean2.num", "2");
      map.put("##bean2.bigNumber", "2000000000");
      map.put("##bean2.firstBeanA", "##bean3");
      map.put("##bean2.secondBeanA", "##bean3");
      map.put("##bean3", "org.jboss.wsf.stack.cxf.client.configuration.BeanA");
      map.put("##bean3.name", "Bean3");
      map.put("##bean3.num", "3");
      map.put("##bean3.pretty", "true");
      map.put("bean2", "invalid");
      map.put("bean3", "invalid");
      map.put("##bean4", "bean4Class");
      return map;
   }

   @Test
   public void testBasicConversion() throws Exception
   {
      Map<String, String> map = getTestMap();
      MapToBeanConverter converter = new MapToBeanConverter(map);
      BeanA bean1 = (BeanA)converter.get("##bean1");
      assertNotNull(bean1);
      assertEquals("Bean1", bean1.getName());
      assertEquals(1, bean1.getNum());
      assertEquals(new Long(1000000000), bean1.getBigNumber());
      assertFalse(bean1.isPretty());
      BeanB bean2 = bean1.getBeanB();
      assertNotNull(bean2);
      assertEquals("Bean2", bean2.getName());
      assertEquals(2, bean2.getNum());
      assertEquals(new Long(2000000000), bean2.getBigNumber());
      BeanA bean3 = bean2.getFirstBeanA();
      assertNotNull(bean3);
      assertEquals("Bean3", bean3.getName());
      assertEquals(3, bean3.getNum());
      assertTrue(bean3.isPretty());
      assertNull(bean3.getBigNumber());
      assertEquals(bean3, bean2.getSecondBeanA());
      assertEquals(bean1, converter.get("##bean1"));
      assertEquals(bean2, converter.get("##bean2"));
      assertEquals(bean3, converter.get("##bean3"));
   }

   @Test
   public void testInvalidGet() throws Exception
   {
      try {
         new MapToBeanConverter(getTestMap()).get("foo");
         fail("IllegalArgumentException expected!");
      } catch (IllegalArgumentException e) {
         assertTrue(e.getMessage().contains("foo"));
      }
   }

   @Test
   public void testMissingClass() throws Exception
   {
      try {
         new MapToBeanConverter(getTestMap()).get("##bean4");
         fail("ClassNotFoundException expected!");
      } catch (ClassNotFoundException e) {
         assertTrue(e.getMessage().contains("bean4Class"));
      }
   }
   
}
