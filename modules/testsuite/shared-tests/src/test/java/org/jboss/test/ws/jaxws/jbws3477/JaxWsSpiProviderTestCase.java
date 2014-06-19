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
import java.util.LinkedList;
import java.util.List;

import junit.framework.Test;

import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * Test JAXWS Spi Provider customization on AS 7
 *
 * @author alessio.soldano@jboss.com
 * @since 02-Apr-2012
 */
public class JaxWsSpiProviderTestCase extends JBossWSTest
{
   private String defaultProvider; 
   
   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-jbws3477-custom-provider.war") { {
         archive
               .addManifest()
               .addAsManifestResource(new File(getTestResourcesDir() + "/jaxws/jbws3477/META-INF/services/javax.xml.ws.spi.Provider"), "services/javax.xml.ws.spi.Provider")
               .addClass(org.jboss.test.ws.jaxws.jbws3477.DummyProvider.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3477.Helper.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3477.TestServlet.class);
         }
      });
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-jbws3477.war") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws3477.Helper.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3477.TestServlet.class);
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }
   
   public static Test suite()
   {
      return new JBossWSTestSetup(JaxWsSpiProviderTestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
   }
   
   protected void setUp() {
      if (isIntegrationCXF()) {
         defaultProvider = "org.jboss.wsf.stack.cxf.client.ProviderImpl";
      }
   }
   
   public void testClientSide()
   {
      Helper.verifyJaxWsSpiProvider(defaultProvider);
   }

   /**
    * Checks the default JAXWS SPI Provider is used (on AS7 that's controlled by the jboss jaxws api, which internally loads org.jboss.ws.jaxws-client module)
    * 
    * @throws Exception
    */
   public void testServerSideDefaultProvider() throws Exception
   {
      runServerTest(new URL("http://" + getServerHost() + ":8080/jaxws-jbws3477?provider=" + defaultProvider));
   }
   
   /**
    * Checks the JAXWS SPI Provider implementation can be overridden on a per-application basis in AS7
    * 
    * @throws Exception
    */
   public void testServerSideProviderCustomization() throws Exception
   {
      runServerTest(new URL("http://" + getServerHost() + ":8080/jaxws-jbws3477-custom-provider?provider=org.jboss.test.ws.jaxws.jbws3477.DummyProvider"));
   }
   
   private static void runServerTest(URL url) throws Exception {
      assertEquals("OK", IOUtils.readAndCloseStream(url.openStream()));
   }
}
