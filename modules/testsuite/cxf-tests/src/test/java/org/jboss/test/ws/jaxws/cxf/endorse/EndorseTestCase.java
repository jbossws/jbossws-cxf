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
package org.jboss.test.ws.jaxws.cxf.endorse;

import java.io.File;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
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
   
   @ArquillianResource
   private URL baseURL;
   
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

   public void testClientSide()
   {
      Helper.verifyCXF();
   }

   @Test
   @RunAsClient
   @OperateOnDeployment(ENDORSE_DEP)
   public void testServerSide() throws Exception
   {
      runServerTest(new URL(baseURL + "?provider=" + ProviderImpl.class.getName()));
   }
   
   @Test
   @RunAsClient
   @OperateOnDeployment(ENDORSE_NO_EXPORT_DEP)
   public void testServerSideNoExport() throws Exception
   {
      runServerTest(new URL(baseURL + "?provider=" + ProviderImpl.class.getName()));
   }
   
   private static void runServerTest(URL url) throws Exception {
      assertEquals("OK", IOUtils.readAndCloseStream(url.openStream()));
   }
}
