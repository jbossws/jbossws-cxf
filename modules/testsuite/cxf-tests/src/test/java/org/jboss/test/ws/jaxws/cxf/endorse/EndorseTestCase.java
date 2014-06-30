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
import java.util.LinkedList;
import java.util.List;

import junit.framework.Test;

import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.stack.cxf.client.ProviderImpl;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;

/**
 * Test required endorsing when using the CXF stack
 *
 * @author alessio.soldano@jboss.com
 * @since 02-Jun-2010
 */
public class EndorseTestCase extends JBossWSTest
{
   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-cxf-endorse.war") { {
         archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client services export\n"))
               .addClass(org.jboss.test.ws.jaxws.cxf.endorse.Helper.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.endorse.TestServlet.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/endorse/WEB-INF/web.xml"));
         }
      });
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-cxf-endorse-no-export.war") { {
         archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client services\n"))
               .addClass(org.jboss.test.ws.jaxws.cxf.endorse.Helper.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.endorse.TestServlet.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/endorse/WEB-INF/web.xml"));
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSCXFTestSetup(EndorseTestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
   }
   
   public void testClientSide()
   {
      Helper.verifyCXF();
   }

   public void testServerSide() throws Exception
   {
      runServerTest(new URL("http://" + getServerHost() + ":8080/jaxws-cxf-endorse?provider=" + ProviderImpl.class.getName()));
   }
   
   public void testServerSideNoExport() throws Exception
   {
      runServerTest(new URL("http://" + getServerHost() + ":8080/jaxws-cxf-endorse-no-export?provider=" + ProviderImpl.class.getName()));
   }
   
   private static void runServerTest(URL url) throws Exception {
      assertEquals("OK", IOUtils.readAndCloseStream(url.openStream()));
   }
}
