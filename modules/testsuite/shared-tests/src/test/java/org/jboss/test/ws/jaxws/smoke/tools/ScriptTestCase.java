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

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Test;
import org.junit.runner.RunWith;

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
@RunWith(Arquillian.class)
public class ScriptTestCase extends JBossWSTest
{
   public static final String FS = System.getProperty("file.separator"); // '/' on unix, '\' on windows
   public static final String PS = System.getProperty("path.separator"); // ':' on unix, ';' on windows
   public static final String EXT = ":".equals( PS ) ? ".sh" : ".bat";

   public String ENDPOINT_CLASS;

   public String JBOSS_HOME;
   public String CLASSES_DIR;
   public String TEST_DIR;

   protected void setUp() throws Exception
   {
      super.setUp();

      JBOSS_HOME = System.getProperty("jboss.home");
      CLASSES_DIR = System.getProperty("test.classes.directory");
      //JBWS-2479
      ENDPOINT_CLASS = "org.jboss.test.ws.jaxws.smoke.tools.CalculatorBean";
      TEST_DIR = createResourceFile("..").getAbsolutePath();
   }

   @Test
   @RunAsClient
   public void testScritpsAvailable() throws Exception
   {
      setUp();
      assertTrue(new File(JBOSS_HOME + FS + "bin" + FS + "wsprovide" + ".sh").exists());
      assertTrue(new File(JBOSS_HOME + FS + "bin" + FS + "wsprovide" + ".bat").exists());
      assertTrue(new File(JBOSS_HOME + FS + "bin" + FS + "wsconsume" + ".sh").exists());
      assertTrue(new File(JBOSS_HOME + FS + "bin" + FS + "wsconsume" + ".bat").exists());
   }
}
