/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
import java.io.InputStreamReader;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;
import org.jboss.wsf.common.IOUtils;

/**
 * [JBWS-1666] Simplify JBossWS jar dependencies
 * 
 * http://jira.jboss.org/jira/browse/JBWS-1666
 *
 * @author Thomas.Diesler@jboss.com
 * @since 14-Jun-2007
 */
public class JBWS1666TestCase extends JBossWSTest
{

   private static final String FS = System.getProperty("file.separator"); // '/' on unix, '\' on windows
   private static final String PS = System.getProperty("path.separator"); // ':' on unix, ';' on windows

   java.util.Properties props = System.getProperties();

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS1666TestCase.class, "jaxws-jbws1666.war");
   }

   public void testPortAccess() throws Exception
   {
      String resStr = TestClient.testPortAccess(getServerHost());
      assertEquals(TestClient.REQ_STR, resStr);
   }

   public void testClientAccess() throws Exception
   {
      File javaFile = new File (System.getProperty("java.home") + FS + "bin" + FS + "java");
      String javaCmd = javaFile.exists() ? javaFile.getCanonicalPath() : "java";
      
      String jbh = System.getProperty("jboss.home");
      String jbc = jbh + FS + "client";
      String jbl = jbh + FS + "lib";
      
      // Setup the classpath - do not modify this lightheartedly. 
      // Maybe you should extend the Class-Path in the MANIFEST instead.
      StringBuffer cp = new StringBuffer(System.getProperty("test.classes.directory"));
      cp.append(PS + jbc + FS + "jbossws-cxf-client.jar");
      cp.append(PS + jbc + FS + "jboss-common-core.jar");
      cp.append(PS + jbc + FS + "jboss-logging-spi.jar");
      cp.append(PS + jbc + FS + "jboss-logging-log4j.jar");
      cp.append(PS + jbc + FS + "jcl-over-slf4j.jar");
      cp.append(PS + jbc + FS + "slf4j-api.jar");
      cp.append(PS + jbc + FS + "slf4j-jboss-logging.jar");
      cp.append(PS + jbc + FS + "jbosssx-client.jar");
      cp.append(PS + jbc + FS + "jboss-javaee.jar");

      Runtime rt = Runtime.getRuntime();

      String command = javaCmd + " -Djava.endorsed.dirs=" + jbl + FS + "endorsed -cp " + cp + " " + TestClient.class.getName() + " " + getServerHost();
      System.out.println("Executing command: " + command);
      Process proc = rt.exec(command);
      int status = proc.waitFor();
      if (status == 0)
      {
         BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
         String resStr = br.readLine();
         assertEquals(TestClient.REQ_STR, resStr);
      }
      else
      {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         IOUtils.copyStream(baos, proc.getErrorStream());
         String errStr = new String(baos.toByteArray());
         fail(errStr);
      }
   }

}
