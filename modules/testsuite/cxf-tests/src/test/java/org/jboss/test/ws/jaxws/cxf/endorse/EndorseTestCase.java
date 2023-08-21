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
package org.jboss.test.ws.jaxws.cxf.endorse;

import java.io.File;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.stack.cxf.client.ProviderImpl;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test required endorsing when using the CXF stack
 *
 * @author alessio.soldano@jboss.com
 * @since 02-Jun-2010
 */
@RunWith(Arquillian.class)
public class EndorseTestCase extends JBossWSTest
{
   private static final String ENDORSE_DEP= "jaxws-cxf-endorse";
   private static final String ENDORSE_NO_EXPORT_DEP= "jaxws-cxf-endorse-no-export";
   
   @Deployment(name = ENDORSE_DEP, testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, ENDORSE_DEP + ".war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client services export\n"))
            .addClass(org.jboss.test.ws.jaxws.cxf.endorse.Helper.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.endorse.TestServlet.class)
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/endorse/WEB-INF/permissions.xml"), "permissions.xml")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/endorse/WEB-INF/web.xml"));
      return archive;
   }

   @Deployment(name = ENDORSE_NO_EXPORT_DEP, testable = false)
   public static WebArchive createDeployment2() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, ENDORSE_NO_EXPORT_DEP + ".war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client services\n"))
            .addClass(org.jboss.test.ws.jaxws.cxf.endorse.Helper.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.endorse.TestServlet.class)
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/endorse/WEB-INF/permissions.xml"), "permissions.xml")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/endorse/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testClientSide()
   {
      Helper.verifyJaxWsSpiProvider(ProviderImpl.class.getName());
      Helper.verifyCXF();
   }

   @Test
   @RunAsClient
   @OperateOnDeployment(ENDORSE_DEP)
   public void testServerSide() throws Exception
   {
      runServerTest(new URL("http://" + getServerHost() + ":" + getServerPort() + "/jaxws-cxf-endorse?provider=" + ProviderImpl.class.getName()));
   }
   
   @Test
   @RunAsClient
   @OperateOnDeployment(ENDORSE_NO_EXPORT_DEP)
   public void testServerSideNoExport() throws Exception
   {
      runServerTest(new URL("http://" + getServerHost() + ":" + getServerPort() + "/jaxws-cxf-endorse-no-export?provider=" + ProviderImpl.class.getName()));
   }
   
   private static void runServerTest(URL url) throws Exception {
      assertEquals("OK", IOUtils.readAndCloseStream(url.openStream()));
   }
}
