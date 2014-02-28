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

import java.net.URL;

import junit.framework.Test;

import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
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
   
   public static Test suite()
   {
      return new JBossWSTestSetup(JaxWsSpiProviderTestCase.class, "jaxws-jbws3477-custom-provider.war,jaxws-jbws3477.war");
   }
   
   protected void setUp() {
      if (isIntegrationCXF()) {
         defaultProvider = "org.jboss.wsf.stack.cxf.client.ProviderImpl";
      }
      else if (isIntegrationNative()) {
         defaultProvider = "org.jboss.ws.core.jaxws.spi.ProviderImpl";
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
