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
package org.jboss.test.ws.jaxws.smoke.tools;

import java.io.File;

import org.jboss.wsf.test.JBossWSTest;

/**
 * [JBWS-1793] Provide a test case for the tools scripts that reside under JBOSS_HOME/bin
 * 
 * http://jira.jboss.org/jira/browse/JBWS-1793
 * 
 * Test the wsprovide and wsconsume scripts that reside
 * under JBOSS_HOME/bin. This basically verifies all dependencies are
 * met to run the shell scripts.
 * 
 * @author Heiko.Braun@jboss.com
 */
public class ScriptTestCase extends JBossWSTest
{
   protected static final String FS = System.getProperty("file.separator"); // '/' on unix, '\' on windows
   protected static final String PS = System.getProperty("path.separator"); // ':' on unix, ';' on windows
   protected static final String EXT = ":".equals( PS ) ? ".sh" : ".bat";

   protected String ENDPOINT_CLASS;

   protected String JBOSS_HOME;
   protected String CLASSES_DIR;
   protected String TEST_DIR;

   protected String origJavaHome;
   
   protected void setUp() throws Exception
   {
      super.setUp();

      JBOSS_HOME = System.getProperty("jboss.home");
      CLASSES_DIR = System.getProperty("test.classes.directory");
      //JBWS-2479
      ENDPOINT_CLASS = "org.jboss.test.ws.jaxws.smoke.tools.CalculatorBean";
      TEST_DIR = createResourceFile("..").getAbsolutePath();
      origJavaHome = System.getProperty("java.home");
      
	  
      // the script requires the system JAVA_HOME, which points to the JDK not the JRE            
	  if(origJavaHome.indexOf(FS + "jre")!=-1)
      {
         String JDK_HOME = origJavaHome.substring(0, origJavaHome.indexOf(FS + "jre"));
         System.setProperty("java.home", JDK_HOME);
      }
   }
   
   public void testScritpsAvailable()
   {
      assertTrue(new File(JBOSS_HOME + FS + "bin" + FS + "wsprovide" + ".sh").exists());
      assertTrue(new File(JBOSS_HOME + FS + "bin" + FS + "wsprovide" + ".bat").exists());
      assertTrue(new File(JBOSS_HOME + FS + "bin" + FS + "wsconsume" + ".sh").exists());
      assertTrue(new File(JBOSS_HOME + FS + "bin" + FS + "wsconsume" + ".bat").exists());
   }

   protected void tearDown() throws Exception
   {
      // reset surefire's JAVA_HOME
      System.setProperty("java.home", origJavaHome);
   }
}
