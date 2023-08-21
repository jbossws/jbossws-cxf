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
package org.jboss.test.ws.jaxws.jbws2591;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jboss.wsf.test.JBossWSTestHelper;

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

   @Before
   public void setup() throws Exception
   {
      JBOSS_HOME = System.getProperty("jboss.home");
      TEST_DIR = createResourceFile("..").getAbsolutePath();
   }


   @Test
   @RunAsClient
   public void testWSConsumeFromCommandLine() throws Exception
   {
      // use absolute path for the output to be re-usable
      String absWsdlLoc = getResourceFile(WSDL_LOCATION).getAbsolutePath();
      String absOutput = new File(TEST_DIR, "wsconsume" + FS + "java").getAbsolutePath();
      String command = JBOSS_HOME + FS + "bin" + FS + "wsconsume" + EXT + " -v -k -o " + absOutput + " " + absWsdlLoc;

      // wildfly9 security manager flag changed from -Djava.security.manager to -secmgr.
      // Can't pass -secmgr arg through arquillian because it breaks arquillian's
      // config of our tests.
      // the -secmgr flag MUST be provided as an input arg to jboss-modules so it must
      // come after the jboss-modules.jar ref.
      String additionalJVMArgs = System.getProperty("additionalJvmArgs", "");
      String securityManagerDesignator = additionalJVMArgs.replace("-Djava.security.manager", "-secmgr");

      File policyFile = new File(JBossWSTestHelper.getTestResourcesDir()
          + "/jaxws/jbws2591/jbws2591-security.policy");
      String securityPolicyFile = " -Djava.security.policy=" + policyFile.getCanonicalPath();

      Map<String, String> env = new HashMap<>();
      env.put("JAVA_OPTS", securityManagerDesignator + securityPolicyFile);

      executeCommand(command, null, "wsconsume", env);
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
