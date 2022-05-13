/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws1666;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import jakarta.xml.ws.spi.Provider;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-1666] Simplify JBossWS jar dependencies
 * [JBWS-3531] Provide testcase for jboss-modules enabled clients
 *
 * @author Thomas.Diesler@jboss.com
 * @author alessio.soldano@jboss.com
 * @since 14-Jun-2007
 */
@RunWith(Arquillian.class)
public class JBWS1666TestCase extends JBossWSTest
{
   private static final String FS = System.getProperty("file.separator"); // '/' on unix, '\' on windows

   java.util.Properties props = System.getProperties();

   @Deployment(name="jaxws-jbws1666", testable = false)
   public static WebArchive createClientDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws1666.war");
      archive
         .addManifest()
         .addClass(org.jboss.test.ws.jaxws.jbws1666.TestEndpointImpl.class)
         .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1666/WEB-INF/web.xml"));
      return archive;
   }

   @Deployment(name = "jaxws-jbws1666-client", testable = false, managed=false)
   public static JavaArchive createClientDeployment1() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws1666-client.jar");
      archive
         .setManifest(new StringAsset("Manifest-Version: 1.0\n"
            + "Main-Class: org.jboss.test.ws.jaxws.jbws1666.TestClient\n"
            + "Dependencies: jakarta.jws.api,jakarta.xml.ws.api, org.eclipse.angus.activation export services\n"))
          .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1666/permissions.xml"), "permissions.xml")
         .addClass(org.jboss.test.ws.jaxws.jbws1666.TestClient.class)
         .addClass(org.jboss.test.ws.jaxws.jbws1666.TestEndpoint.class);
      JBossWSTestHelper.writeToFile(archive);
      return archive;
   }

   @Deployment(name = "jaxws-jbws1666-b-client", testable = false, managed = false)
   public static JavaArchive createClientDeployment2() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws1666-b-client.jar");
      archive
         .setManifest(new StringAsset("Manifest-Version: 1.0\n"
            + "Main-Class: org.jboss.test.ws.jaxws.jbws1666.TestClient\n"
            + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client export services\n"))
          .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1666/permissions.xml"), "permissions.xml")
         .addClass(org.jboss.test.ws.jaxws.jbws1666.TestClient.class)
         .addClass(org.jboss.test.ws.jaxws.jbws1666.TestEndpoint.class);
      JBossWSTestHelper.writeToFile(archive);
      return archive;
   }

   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-jbws1666")
   public void testClientInTestsuiteJVM() throws Exception
   {
      String resStr = TestClient.testPortAccess(getServerHost(), getServerPort());
      assertEquals(TestClient.REQ_STR, resStr);
   }
   
   @Test
   @RunAsClient
   public void testClientUsingJBossModules() throws Exception {
      runJBossModulesClient("jaxws-jbws1666-client.jar");
   }

   @Test
   @RunAsClient
   public void testClientUsingJBossModulesWithJBossWSClientAggregationModule() throws Exception {
      if (!isIntegrationCXF()) {
         return;
      }
      runJBossModulesClient("jaxws-jbws1666-b-client.jar");
   }

   private void runJBossModulesClient(String clientJar) throws Exception {

      StringBuilder sbuf = new StringBuilder();

      // java cmd
      File javaFile = new File (System.getProperty("java.home") + FS + "bin" + FS + "java");
      String javaCmd = javaFile.exists() ? javaFile.getCanonicalPath() : "java";
      sbuf.append(javaCmd);

      //properties
      String additionalJVMArgs = System.getProperty("additionalJvmArgs", "");
      additionalJVMArgs =  additionalJVMArgs.replace('\n', ' ');
      sbuf.append(" ").append(additionalJVMArgs);
      sbuf.append(" -Dlog4j.output.dir=").append(System.getProperty("log4j.output.dir"));

      final String jbh = System.getProperty("jboss.home");
      final String jbm = jbh + FS + "modules";
      final String jbmjar = jbh + FS + "jboss-modules.jar";
      sbuf.append(" -jar ").append(jbmjar);

      // input arguments to jboss-module's main
      sbuf.append(" -mp ").append(jbm);

      // wildfly9 security manage flag changed from -Djava.security.manager to -secmgr.
      // Can't pass -secmgr arg through arquillian because it breaks arquillian's
      // config of our tests.
      // the -secmgr flag MUST be provided as an input arg to jboss-modules so it must
      // come after the jboss-modules.jar ref.
      if (additionalJVMArgs.contains("-Djava.security.manager")) {
         sbuf.append(" ").append("-secmgr");
      }

      // our client jar is an input param to jboss-module
      final File f = new File(JBossWSTestHelper.getTestArchiveDir(), clientJar);
      sbuf.append(" -jar ").append(f.getAbsolutePath());

      // input args to our client.jar main
      sbuf.append(" ").append(getServerHost()).append(" ").append(getServerPort());

      final String command = sbuf.toString();
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      executeCommand(command, bout);
      //check result (includes check on Provider impl, which might be affected by missing jakarta.xml.ws.api module dependency
      assertEquals(Provider.provider().getClass().getName() + ", " + TestClient.REQ_STR, readFirstLine(bout));
   }

   private static String readFirstLine(ByteArrayOutputStream bout) throws IOException {
      bout.flush();
      final byte[] bytes = bout.toByteArray();
      if (bytes != null) {
          BufferedReader reader = new BufferedReader(new java.io.StringReader(new String(bytes)));
          return reader.readLine();
      } else {
         return null;
      }
   }
}
