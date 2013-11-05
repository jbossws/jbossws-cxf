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

import javax.xml.ws.spi.Provider;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBWS-1666] Simplify JBossWS jar dependencies
 * [JBWS-3531] Provide testcase for jboss-modules enabled clients
 *
 * @author Thomas.Diesler@jboss.com
 * @author alessio.soldano@jboss.com
 * @since 14-Jun-2007
 */
public class JBWS1666TestCase extends JBossWSTest
{
   private static final String FS = System.getProperty("file.separator"); // '/' on unix, '\' on windows

   java.util.Properties props = System.getProperties();
   
   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS1666TestCase.class, "jaxws-jbws1666.war");
   }

   public void testClientInTestsuiteJVM() throws Exception
   {
      String resStr = TestClient.testPortAccess(getServerHost());
      assertEquals(TestClient.REQ_STR, resStr);
   }
   
   public void testClientUsingJBossModules() throws Exception {
      runJBossModulesClient("jaxws-jbws1666-client.jar");
   }

   public void testClientUsingJBossModulesWithJBossWSClientAggregationModule() throws Exception {
      if (!isIntegrationCXF()) {
         return;
      }
      runJBossModulesClient("jaxws-jbws1666-b-client.jar");
   }
   
   private void runJBossModulesClient(String clientJar) throws Exception {
      File javaFile = new File (System.getProperty("java.home") + FS + "bin" + FS + "java");
      String javaCmd = javaFile.exists() ? javaFile.getCanonicalPath() : "java";
      
      final String jbh = System.getProperty("jboss.home");
      final String jbm = jbh + FS + "modules";
      final String jbmjar = jbh + FS + "jboss-modules.jar";
      
      final File f = new File(JBossWSTestHelper.getTestArchiveDir(), clientJar);

      //java -jar $JBOSS_HOME/jboss-modules.jar -mp $JBOSS_HOME/modules -jar client.jar
      String props = " -Dlog4j.output.dir=" + System.getProperty("log4j.output.dir") + " -jar " + jbmjar + " -mp " + jbm; 
      final String command = javaCmd + props + " -jar " + f.getAbsolutePath() + " " + getServerHost();
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      executeCommand(command, bout);
      //check result (includes check on Provider impl, which might be affected by missing javax.xml.ws.api module dependency
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
