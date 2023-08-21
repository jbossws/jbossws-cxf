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
package org.jboss.test.ws.jaxws.jbws2268;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-2268] Implement @PostConstruct and @Predestroy annotations support for POJO based endpoints
 * @author richard.opalka@jboss.com
 */
@RunWith(Arquillian.class)
public final class JBWS2268TestCase extends JBossWSTest
{
   @ArquillianResource
   Deployer deployer;

   @Deployment(name = "dep", testable = false, managed = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws2268.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws2268.EndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2268.EndpointInterface.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2268/META-INF/permissions.xml"), "permissions.xml")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2268/WEB-INF/web.xml"));
      return archive;
   }

   private EndpointInterface getProxy() throws Exception
   {
      QName serviceName = new QName("http://www.jboss.org/test/ws/jaxws/jbws2268", "EndpointService");
      URL wsdlURL = new URL("http://" + getServerHost() + ":" + getServerPort() + "/jaxws-jbws2268?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      return (EndpointInterface)service.getPort(EndpointInterface.class);
   }
   
   private File createLogFile() throws Exception
   {
      File workDir = getResourceFile("target");
      assertTrue("Work dir doesn't exist", workDir.exists());
      String fileName = System.identityHashCode(this) + ".log";
      File retVal = new File(workDir, fileName);
      retVal.createNewFile();
      return retVal;
   }

   @Test
   @RunAsClient
   @OperateOnDeployment("dep")
   public void testJavaxAnnotationsSupport() throws Exception
   {
      File logFile = createLogFile();

      deployer.deploy("dep");
      try
      {
         assertTrue(getProxy().setFile(logFile.getAbsolutePath()));
      }
      finally
      {
         deployer.undeploy("dep");
         assertPostConstructAndPreDestroyLogs(logFile);
      }
   }
   
   private void assertPostConstructAndPreDestroyLogs(File logFile) throws Exception
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      IOUtils.copyStream(baos, new FileInputStream(logFile));
      assertEquals("init() destroy()", baos.toString().trim());
      logFile.delete();
   }

}
