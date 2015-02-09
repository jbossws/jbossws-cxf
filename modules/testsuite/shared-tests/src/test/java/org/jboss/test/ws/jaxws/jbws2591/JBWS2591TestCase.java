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
package org.jboss.test.ws.jaxws.jbws2591;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-2591] WSConsume does not generate @XmlList with doc/lit wsdl
 * 
 * http://jira.jboss.org/jira/browse/JBWS-2591
 * 
 * @author alessio.soldano@jboss.com
 * @since 01-Apr-2009
 */
@RunWith(Arquillian.class)
public class JBWS2591TestCase extends JBossWSTest
{
   private static final String FS = System.getProperty("file.separator"); // '/' on unix, '\' on windows
   private static final String PS = System.getProperty("path.separator"); // ':' on unix, ';' on windows
   private static final String EXT = ":".equals(PS) ? ".sh" : ".bat";

   private String WSDL_LOCATION = "jaxws" + FS + "jbws2591" + FS + "wsdl" + FS + "JBWS2591TestService.wsdl";

   private String JBOSS_HOME;
   private String TEST_DIR;

   protected void setUp() throws Exception
   {
      super.setUp();

      JBOSS_HOME = System.getProperty("jboss.home");
      TEST_DIR = createResourceFile("..").getAbsolutePath();
   }


   @Test
   @RunAsClient
   public void testWSConsumeFromCommandLine() throws Exception
   {
      setUp();
      // use absolute path for the output to be re-usable
      String absWsdlLoc = getResourceFile(WSDL_LOCATION).getAbsolutePath();
      String absOutput = new File(TEST_DIR, "wsconsume" + FS + "java").getAbsolutePath();
      String command = JBOSS_HOME + FS + "bin" + FS + "wsconsume" + EXT + " -v -k -o " + absOutput + " " + absWsdlLoc;
      executeCommand(command, "wsconsume");
      File javaSource = new File(TEST_DIR, "wsconsume" + FS + "java" + FS + "org" + FS + "marshalltestservice" + FS + "newschemadefs" + FS + "NewSchemaTest.java");
      assertTrue("Service endpoint interface not generated", javaSource.exists());
      String contents = readFile(javaSource);
      assertTrue("@XmlList not found", contents.contains("@XmlList"));
   }

   private String readFile(File file) throws Exception
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
