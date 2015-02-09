/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
   protected String origLog4jConf;
   protected String oldCPProp;
   
   private static final String LOG4J_CONF = "log4j.configuration";

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
      delegate = clazz.newInstance();
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
      // We also need to explicitly set the log4j.configuration sys prop to the current log4j.xml resource url
      // as changing the current classloader results in a log4j configuration coming from thirdparty lib being used. 
      jarFirstClasspath.addAll(jarURLs);
      jarFirstClasspath.addAll(classDirUrls);
      this.origClassLoader = Thread.currentThread().getContextClassLoader();
      URLClassLoader jarFirstClassLoader = new URLClassLoader(jarFirstClasspath.toArray( new URL[] {}), this.origClassLoader);

      URL log4jXmlUrl = this.origClassLoader.getResource("log4j.xml");
      this.origLog4jConf = System.getProperty(LOG4J_CONF);

      Thread.currentThread().setContextClassLoader(jarFirstClassLoader);
      this.oldCPProp = System.getProperty("java.class.path");
      System.setProperty("java.class.path", jarURLString.toString());
      if (log4jXmlUrl != null)
         System.setProperty(LOG4J_CONF, log4jXmlUrl.toString());
   }

   protected void restoreClasspath()
   {
      if(this.origClassLoader !=null)
      {
         Thread.currentThread().setContextClassLoader(this.origClassLoader);
         this.origClassLoader = null;
         System.setProperty("java.class.path", oldCPProp);
         if (origLog4jConf != null)
         {
            System.setProperty(LOG4J_CONF, origLog4jConf);
         }
         else
         {
            System.clearProperty(LOG4J_CONF);
         }
      }
   }
}
