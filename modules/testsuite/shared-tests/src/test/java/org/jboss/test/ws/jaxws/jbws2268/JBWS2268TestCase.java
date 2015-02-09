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
