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
package org.jboss.wsf.stack.cxf.client;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * 
 * @author alessio.soldano@jboss.com
 * @since 03-Oct-2013
 *
 */
class SecurityActions
{
   /**
    * Get context classloader.
    *
    * @return the current context classloader
    */
   static ClassLoader getContextClassLoader()
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm == null)
      {
         return Thread.currentThread().getContextClassLoader();
      }
      else
      {
         return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>()
         {
            public ClassLoader run()
            {
               return Thread.currentThread().getContextClassLoader();
            }
         });
      }
   }
   
   /**
    * Set context classloader.
    *
    * @param classLoader the classloader
    */
   static void setContextClassLoader(final ClassLoader classLoader)
   {
      if (System.getSecurityManager() == null)
      {
         Thread.currentThread().setContextClassLoader(classLoader);
      }
      else
      {
         AccessController.doPrivileged(new PrivilegedAction<Object>()
         {
            public Object run()
            {
               Thread.currentThread().setContextClassLoader(classLoader);
               return null;
            }
         });
      }
   }

   static ClassLoader getClassLoader(Class clazz)
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm == null)
      {
         return clazz.getClassLoader();
      }
      else
      {
         return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>()
         {
            public ClassLoader run()
            {
               return clazz.getClassLoader();
            }
         });
      }
   }

   
   /**
    * Return the current value of the specified system property
    * 
    * @param name
    * @param defaultValue
    * @return
    */
   static String getSystemProperty(final String name, final String defaultValue)
   {
      PrivilegedAction<String> action = new PrivilegedAction<String>()
      {
         public String run()
         {
            return System.getProperty(name, defaultValue);
         }
      };
      return AccessController.doPrivileged(action);
   }
   
   
   static Boolean getBoolean(final String propName, final Boolean defaultValue)
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm == null)
      {
         String s = System.getProperty(propName);
         return (s != null) ? Boolean.valueOf(s) : defaultValue;
      }
      else
      {
         return AccessController.doPrivileged(new PrivilegedAction<Boolean>()
         {
            public Boolean run()
            {
               String s = getSystemProperty(propName, null);
               return (s != null) ? Boolean.valueOf(s) : defaultValue;
            }
         });
      }
   } 
}
