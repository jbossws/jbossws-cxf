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
package org.jboss.test.ws.jaxws.jbws2529;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.ws.common.DOMUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Element;
import org.jboss.wsf.test.JBossWSTestHelper;

/** 
 * [JBWS-2529] Missing type in generated WSDL part definition
 * 
 * http://jira.jboss.org/jira/browse/JBWS-2529
 * 
 * @author alessio.soldano@jboss.com
 * @since 13-Mar-2009
 */
@RunWith(Arquillian.class)
public class JBWS2529TestCase extends JBossWSTest
{
   private final String FS = System.getProperty("file.separator"); // '/' on unix, '\' on windows
   private final String PS = System.getProperty("path.separator"); // ':' on unix, ';' on windows
   private final String EXT = ":".equals( PS ) ? ".sh" : ".bat";
   private final String XML_SCHEMA_NS = "http://www.w3.org/2001/XMLSchema";

   private String ENDPOINT_CLASS;

   private String JBOSS_HOME;
   private String CLASSES_DIR;
   private String TEST_DIR;

   @Before
   public void setup() throws Exception
   {
      JBOSS_HOME = System.getProperty("jboss.home");
      CLASSES_DIR = System.getProperty("test.classes.directory");
      ENDPOINT_CLASS = "org.jboss.test.ws.jaxws.jbws2529.JBWS2529Endpoint";
      TEST_DIR = createResourceFile("..").getAbsolutePath();
   }

   @Test
   @RunAsClient
   public void test() throws Exception
   {
      File destDir = new File(TEST_DIR, "wsprovide" + FS + "java");
      String absOutput = destDir.getAbsolutePath();
      String command = JBOSS_HOME + FS + "bin" + FS + "wsprovide" + EXT + " -k -w -o " + absOutput + " --classpath " + CLASSES_DIR + " " + ENDPOINT_CLASS;

      // wildfly9 security manager flag changed from -Djava.security.manager to -secmgr.
      // Can't pass -secmgr arg through arquillian because it breaks arquillian's
      // config of our tests.
      // the -secmgr flag MUST be provided as an input arg to jboss-modules so it must
      // come after the jboss-modules.jar ref.
      String additionalJVMArgs = System.getProperty("additionalJvmArgs", "");
      String securityManagerDesignator = additionalJVMArgs.replace("-Djava.security.manager", "-secmgr");

      File policyFile = new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2529/jbws2529-security.policy");
      String securityPolicyFile = " -Djava.security.policy=" + policyFile.getCanonicalPath();

      Map<String, String> env = new HashMap<>();
      env.put("JAVA_OPTS", securityManagerDesignator + securityPolicyFile);
      executeCommand(command, null, "wsprovide", env);
      
      File wsdl = new File(destDir, isIntegrationCXF() ? "JBWS2529EndpointService_schema1.xsd" : "JBWS2529EndpointService.wsdl");
      Element root = DOMUtils.parse(new FileInputStream(wsdl));
      QName schemaQName = new QName(XML_SCHEMA_NS,"schema");
      Element schema = null;
      if (root.getLocalName().equals(schemaQName.getLocalPart()) && root.getNamespaceURI().equals(schemaQName.getNamespaceURI()))
      {
         schema = root;
      }
      else
      {
         schema = (Element)DOMUtils.getChildElements(root, new QName(XML_SCHEMA_NS,"schema"), true).next();
      }
      List<Element> elements = DOMUtils.getChildElementsAsList(schema, new QName(XML_SCHEMA_NS,"element"));
      boolean foundRequest = false;
      boolean foundResponse = false;
      for (Element el : elements)
      {
         if ("helloWrapped".equals(el.getAttribute("name")))
            foundRequest = true;
         if ("helloWrappedResponse".equals(el.getAttribute("name")))
            foundResponse = true;
      }
      assertTrue("helloWrapped element not found!", foundRequest);
      assertTrue("helloWrappedResponse element not found!", foundResponse);
   }

}
