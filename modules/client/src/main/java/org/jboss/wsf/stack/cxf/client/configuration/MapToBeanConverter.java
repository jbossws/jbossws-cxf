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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.BeanUtils;
import org.jboss.ws.common.utils.DelegateClassLoader;
import org.jboss.wsf.spi.classloading.ClassLoaderProvider;


/**
 * Converts a properties map (String to String) into bean(s).
 * 
 * key0          |   Fdfdsfs
 * key1          |   ##foo
 * ##foo         |   com.company.Foo
 * ##foo.par1    |   AbcDe
 * ##foo.par2    |   ##par
 * ##par         |   com.company.Bar
 * ##par.name    |   John
 * ##par.surname |   Black
 * ##foo.par3    |   ##par
 * 
 * A call to get("key1") returns the same you would get with:
 * 
 * com.company.Foo foo = new com.company.Foo();
 * foo.setPar1("AbcDe");
 * com.company.Bar par = new com.company.Bar();
 * par.setName("John");
 * par.setSurname("Black");
 * foo.setPar2(par);
 * foo.setPar3(par);
 * return foo;
 * 
 * 
 * @author alessio.soldano@jboss.com
 * @since 11-Mar-2015
 *
 */
public class MapToBeanConverter
{
   public static final String BEAN_ID_PREFIX = "##";
   public static final String DOT = ".";
   private final Map<String, String> map;
   private final Map<String, Object> builtObjectsMap;
   
   public MapToBeanConverter(Map<String, String> map) {
      this.map = map;
      this.builtObjectsMap = new HashMap<String, Object>();
   }
   
   public Object get(String key) throws Exception {
      if (key == null || !key.startsWith(BEAN_ID_PREFIX)) {
         throw new IllegalArgumentException("Provided key does not start with " + BEAN_ID_PREFIX + ": " + key);
      }
      Object result = builtObjectsMap.get(key);
      if (result == null) {
         result = build(key);
         builtObjectsMap.put(key, result);
      }
      return result;
   }
   
   protected Object build(String key) throws Exception {
      Object bean = newInstance(map.get(key));
      Map<String, String> attributes = attributesByBeanRef(key);
      if (!attributes.isEmpty()) {
         for (Entry<String, String> e : attributes.entrySet()) {
            final String v = e.getValue();
            BeanUtils.setProperty(bean, e.getKey(), v.startsWith(BEAN_ID_PREFIX) ? get(v) : v);
         }
      }
      return bean;
   }
   
   protected Object newInstance(String className) throws Exception {
      ClassLoader loader = new DelegateClassLoader(ClassLoaderProvider.getDefaultProvider()
            .getServerIntegrationClassLoader(), SecurityActions.getContextClassLoader());
      Class<?> clazz = SecurityActions.loadClass(loader, className);
      return clazz.newInstance();
   }
   
   /**
    * Return an attribute name to attribute value map
    * 
    * @param beanRef
    * @return
    */
   protected Map<String, String> attributesByBeanRef(String beanRef) {
      Map<String, String> result = null;
      for (Entry<String, String> e : map.entrySet()) {
         final String k = e.getKey();
         if (k.startsWith(beanRef) && k.startsWith(DOT, beanRef.length())) {
            if (result == null) {
               result = new HashMap<String,String>();
            }
            result.put(k.substring(beanRef.length() + DOT.length()), e.getValue());
         }
      }
      if (result == null) {
         return Collections.emptyMap();
      } else {
         return result;
      }
   }
   
}