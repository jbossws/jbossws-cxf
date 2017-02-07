/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
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
