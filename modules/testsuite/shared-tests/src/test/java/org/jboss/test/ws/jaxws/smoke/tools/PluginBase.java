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
package org.jboss.test.ws.jaxws.smoke.tools;

import org.jboss.wsf.test.JBossWSTest;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.io.File;

/**
 * @author Heiko.Braun <heiko.braun@jboss.com>
 */
public abstract class PluginBase extends JBossWSTest
{
   protected Object delegate = null;
   protected ClassLoader origClassLoader;
   protected String oldCPProp;


   protected void dispatch(String methodName) throws Exception
   {
      try
      {
         delegate.getClass().getMethod(methodName).invoke(delegate);
      }
      catch (InvocationTargetException e)
      {
         e.printStackTrace();
         throw e;
      }
   }
   
   protected void setDelegate(Class<?> clazz) throws Exception
   {
      delegate = clazz.getDeclaredConstructor().newInstance();
      List<String> list = new LinkedList<String>();
      for (Class<?> c : clazz.getInterfaces())
      {
         list.add(c.getName());
      }
   }

   protected void setupClasspath() throws Exception
   {
      String classpath = System.getProperty("surefire.test.class.path");
      if (classpath == null) //no maven surefire classpath hacks required
         return;
      List<URL> jarURLs = new LinkedList<URL>();
      StringBuffer jarURLString = new StringBuffer();
      List<URL> classDirUrls = new LinkedList<URL>();

      if (classpath != null && !classpath.equals(""))
      {
         StringTokenizer st = new StringTokenizer(classpath, File.pathSeparator, false);
         while (st.hasMoreTokens())
         {
            String s = st.nextToken();
            if(s.endsWith(".jar"))  // JBWS-2175: skip target/classes and target/test-classes
            {
               jarURLs.add( new File(s).toURI().toURL() );
               jarURLString.append( s ).append(File.pathSeparator);
            }
            else
            {
               classDirUrls.add( new File(s).toURI().toURL() );
            }
         }

      }

      List<URL> jarFirstClasspath = new ArrayList<URL>();

	   // Replace the ThreadContextLoader to prevent loading from target/classes and target/test-classes.
       jarFirstClasspath.addAll(jarURLs);
      jarFirstClasspath.addAll(classDirUrls);
      this.origClassLoader = Thread.currentThread().getContextClassLoader();
      URLClassLoader jarFirstClassLoader = new URLClassLoader(jarFirstClasspath.toArray( new URL[] {}), this.origClassLoader);
      Thread.currentThread().setContextClassLoader(jarFirstClassLoader);
      this.oldCPProp = System.getProperty("java.class.path");
      System.setProperty("java.class.path", jarURLString.toString());
   }

   protected void restoreClasspath()
   {
      if(this.origClassLoader !=null)
      {
         Thread.currentThread().setContextClassLoader(this.origClassLoader);
         this.origClassLoader = null;
         System.setProperty("java.class.path", oldCPProp);
      }
   }
}
