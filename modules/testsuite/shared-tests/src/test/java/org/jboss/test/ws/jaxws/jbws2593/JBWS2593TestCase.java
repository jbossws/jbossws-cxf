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
package org.jboss.test.ws.jaxws.jbws2593;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.jboss.wsf.test.JBossWSTest;

/**
 * [JBWS-2593] WSConsume does not generate @XmlJavaTypeAdapter in SEI
 * 
 * http://jira.jboss.org/jira/browse/JBWS-2593
 * 
 * @author alessio.soldano@jboss.com
 * @since 02-Apr-2009
 */
public class JBWS2593TestCase extends JBossWSTest
{
   private static final String FS = System.getProperty("file.separator"); // '/' on unix, '\' on windows
   private static final String PS = System.getProperty("path.separator"); // ':' on unix, ';' on windows
   private static final String EXT = ":".equals(PS) ? ".sh" : ".bat";

   private String WSDL_LOCATION_RPC = "jaxws" + FS + "jbws2593" + FS + "wsdl" + FS + "JBWS2593TestRPCService.wsdl";
   
   private String WSDL_LOCATION_DOC = "jaxws" + FS + "jbws2593" + FS + "wsdl" + FS + "JBWS2593TestDOCService.wsdl";

   private String JBOSS_HOME;
   private String TEST_DIR;

   private String origJavaHome;

   protected void setUp() throws Exception
   {
      super.setUp();

      JBOSS_HOME = System.getProperty("jboss.home");
      TEST_DIR = createResourceFile("..").getAbsolutePath();
      origJavaHome = System.getProperty("java.home");

      // the script requires the system JAVA_HOME, which points to the JDK not the JRE            
      if (origJavaHome.indexOf(FS + "jre") != -1)
      {
         String JDK_HOME = origJavaHome.substring(0, origJavaHome.indexOf(FS + "jre"));
         System.setProperty("java.home", JDK_HOME);
      }
   }

   protected void tearDown() throws Exception
   {
      // reset surefire's JAVA_HOME
      System.setProperty("java.home", origJavaHome);
   }
   
   public void testRPC() throws Exception
   {
      this.internalTest(true);
   }
   
   public void testDOC() throws Exception
   {
      this.internalTest(false);
   }

   private void internalTest(boolean rpc) throws Exception
   {
      // use absolute path for the output to be re-usable
      String absWsdlLoc = getResourceFile(rpc ? WSDL_LOCATION_RPC : WSDL_LOCATION_DOC).getAbsolutePath();
      String absOutput = new File(TEST_DIR, "wsconsume" + FS + "java").getAbsolutePath();
      String command = JBOSS_HOME + FS + "bin" + FS + "wsconsume" + EXT + " -v -k -o " + absOutput + " " + absWsdlLoc;
      executeCommand(command, "wsconsume");
      File javaSource = new File(TEST_DIR, "wsconsume" + FS + "java" + FS + "org" + FS + "jbws2593_" + (rpc ? "rpc" : "doc") + FS + "ParameterModeTest.java");
      assertTrue("Service endpoint interface not generated", javaSource.exists());
      String contents = readFile(javaSource);
      assertEquals(2, countOccurrences(contents, "@XmlJavaTypeAdapter"));
      assertEquals(2, countOccurrences(contents, "HexBinaryAdapter.class"));
   }

   public static int countOccurrences(String string, String textToSearchFor)
   {
      int count = 0;
      int index = 0;
      while ((index = string.indexOf(textToSearchFor, index)) != -1)
      {
         ++index;
         ++count;
      }
      return count;
   }

   private static String readFile(File file) throws Exception
   {
      BufferedReader input = new BufferedReader(new FileReader(file));
      StringBuilder sb = new StringBuilder();
      try
      {
         String line = null;
         while ((line = input.readLine()) != null)
         {
            sb.append(line);
            sb.append(System.getProperty("line.separator"));
         }
      }
      finally
      {
         input.close();
      }
      return sb.toString();
   }

}
