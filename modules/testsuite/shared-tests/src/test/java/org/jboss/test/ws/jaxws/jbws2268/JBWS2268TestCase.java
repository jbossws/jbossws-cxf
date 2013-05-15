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
package org.jboss.test.ws.jaxws.jbws2268;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBWS-2268] Implement @PostConstruct and @Predestroy annotations support for POJO based endpoints
 * @author richard.opalka@jboss.com
 */
public final class JBWS2268TestCase extends JBossWSTest
{

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS2268TestCase.class, "");
   }

   private EndpointInterface getProxy() throws Exception
   {
      QName serviceName = new QName("http://www.jboss.org/test/ws/jaxws/jbws2268", "EndpointService");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws2268?wsdl");

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

   public void testJavaxAnnotationsSupport() throws Exception
   {
      File logFile = createLogFile();

      deploy("jaxws-jbws2268.war");
      try
      {
         assertTrue(getProxy().setFile(logFile.getAbsolutePath()));
      }
      finally
      {
         undeploy("jaxws-jbws2268.war");
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
