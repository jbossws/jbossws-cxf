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
import java.util.HashMap;
import java.util.Map;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
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
public class WSConsumeScriptTestCase extends org.jboss.test.ws.jaxws.smoke.tools.ScriptTestCase
{
   private String WSDL_LOCATION = "jaxws" + FS + "smoke" + FS + "tools" + FS + "wsdl" + FS + "TestServiceCatalog.wsdl";

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

      Map<String, String> env = new HashMap<>();
      env.put("JAVA_OPTS", securityManagerDesignator);
      executeCommand(command, null, "wsconsume", env);
      File javaSource = new File(TEST_DIR, "wsconsume" + FS + "java" + FS + "org" + FS + "openuri" + FS + "_2004" + FS + "_04" + FS + "helloworld" + FS + "EndpointInterface.java");
      assertTrue("Service endpoint interface not generated", javaSource.exists());
   }
}
