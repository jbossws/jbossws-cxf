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
package org.jboss.test.ws.jaxws.jbws3477;

import static org.jboss.wsf.test.JBossWSTestHelper.getTestResourcesDir;

import java.io.File;
import java.net.URL;

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
 * Test JAXWS Spi Provider customization on AS 7
 *
 * @author alessio.soldano@jboss.com
 * @since 02-Apr-2012
 */
@RunWith(Arquillian.class)
public class JaxWsSpiProviderTestCase extends JBossWSTest
{
   private String defaultProvider = "org.jboss.wsf.stack.cxf.client.ProviderImpl";

   @ArquillianResource
   private URL baseURL;

   @Deployment(name="jaxws-jbws3477-custom-provider", order=1, testable = false)
   public static WebArchive createDeployment1() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws3477-custom-provider.war");
      archive
         .addManifest()
         .addAsManifestResource(new File(getTestResourcesDir() + "/jaxws/jbws3477/META-INF/services/jakarta.xml.ws.spi.Provider"), "services/jakarta.xml.ws.spi.Provider")
         .addClass(org.jboss.test.ws.jaxws.jbws3477.DummyProvider.class)
         .addClass(org.jboss.test.ws.jaxws.jbws3477.Helper.class)
         .addClass(org.jboss.test.ws.jaxws.jbws3477.TestServlet.class)
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3477/META-INF/permissions.xml"), "permissions.xml");
      return archive;
   }

   @Deployment(name="jaxws-jbws3477", order=2, testable = false)
   public static WebArchive createDeployment2() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws3477.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws3477.Helper.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3477.TestServlet.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3477/META-INF/permissions.xml"), "permissions.xml");
      return archive;
   }

   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-jbws3477")
   public void testClientSide()
   {
      Helper.verifyJaxWsSpiProvider(defaultProvider);
   }

   /**
    * Checks the default JAXWS SPI Provider is used (on AS7 that's controlled by the jboss jaxws api, which internally loads org.jboss.ws.jaxws-client module)
    * 
    * @throws Exception
    */
   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-jbws3477")
   public void testServerSideDefaultProvider() throws Exception
   {
      runServerTest(new URL(baseURL + "?provider=" + defaultProvider));
   }
   
   /**
    * Checks the JAXWS SPI Provider implementation can be overridden on a per-application basis in AS7
    * 
    * @throws Exception
    */
   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-jbws3477-custom-provider")
   public void testServerSideProviderCustomization() throws Exception
   {
      runServerTest(new URL(baseURL + "?provider=org.jboss.test.ws.jaxws.jbws3477.DummyProvider"));
   }
   
   private static void runServerTest(URL url) throws Exception {
      assertEquals("OK", IOUtils.readAndCloseStream(url.openStream()));
   }
}
