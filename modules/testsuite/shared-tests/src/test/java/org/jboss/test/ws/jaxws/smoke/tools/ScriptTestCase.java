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

import java.io.File;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Before;
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

   @Before
   public void setup() throws Exception
   {
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
      assertTrue(new File(JBOSS_HOME + FS + "bin" + FS + "wsprovide" + ".sh").exists());
      assertTrue(new File(JBOSS_HOME + FS + "bin" + FS + "wsprovide" + ".bat").exists());
      assertTrue(new File(JBOSS_HOME + FS + "bin" + FS + "wsconsume" + ".sh").exists());
      assertTrue(new File(JBOSS_HOME + FS + "bin" + FS + "wsconsume" + ".bat").exists());
   }
}
