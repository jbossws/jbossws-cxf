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
import java.util.HashMap;
import java.util.Map;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jboss.wsf.test.JBossWSTestHelper;

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
public class WSProvideScriptTestCase extends org.jboss.test.ws.jaxws.smoke.tools.ScriptTestCase
{
   @Test
   @RunAsClient
   public void testWSProvideFromCommandLine() throws Exception
   {
      String absOutput = new File(TEST_DIR, "wsprovide" + FS + "java").getAbsolutePath();
      String command = JBOSS_HOME + FS + "bin" + FS + "wsprovide" + EXT + " -k -w -o " + absOutput + " --classpath " + CLASSES_DIR + " " + ENDPOINT_CLASS;

      // wildfly9 security manager flag changed from -Djava.security.manager to -secmgr.
      // Can't pass -secmgr arg through arquillian because it breaks arquillian's
      // config of our tests.
      // the -secmgr flag MUST be provided as an input arg to jboss-modules so it must
      // come after the jboss-modules.jar ref.
      String additionalJVMArgs = System.getProperty("additionalJvmArgs", "");
      String securityManagerDesignator = additionalJVMArgs.replace("-Djava.security.manager", "-secmgr");

      File policyFile = new File(JBossWSTestHelper.getTestResourcesDir()
          + "/jaxws/smoke/tools/WSProvideScriptTestCase-security.policy");
      String securityPolicyFile = " -Djava.security.policy=" + policyFile.getCanonicalPath();

      Map<String, String> env = new HashMap<>();
      env.put("JAVA_OPTS", securityManagerDesignator + securityPolicyFile);

      executeCommand(command, null, "wsprovide", env);
      File javaSource = new File(TEST_DIR, "wsprovide" + FS + "java" + FS + "org" + FS + "jboss" + FS + "test" + FS + "ws" + FS + "jaxws" + FS + "smoke" + FS + "tools" + FS + "jaxws" + FS + "AddResponse.java");
      assertTrue("Response wrapper not generated", javaSource.exists());
   }

}
