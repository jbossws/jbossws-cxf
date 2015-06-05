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
         .addAsManifestResource(new File(getTestResourcesDir() + "/jaxws/jbws3477/META-INF/services/javax.xml.ws.spi.Provider"), "services/javax.xml.ws.spi.Provider")
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
