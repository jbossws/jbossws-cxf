/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.clientConfig;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Test;

import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestSetup;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;

/**
 * Verifies client configuration setup (in-container tests, relying on AS model)
 *
 * @author alessio.soldano@jboss.com
 * @since 31-May-2012
 */
public class ClientConfigurationTestCaseForked extends JBossWSTest
{
   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.JarDeployment("jaxws-clientConfig-client.jar") { {
         archive
               .addManifest()
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/clientConfig/META-INF/jaxws-client-config.xml"), "jaxws-client-config.xml");
         }
      });
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-clientConfig-inContainer-client.war") { {
         archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: org.jboss.ws.common\n"))
               .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/clientConfig/META-INF/jaxws-client-config.xml"), "META-INF/jaxws-client-config.xml")
               .addClass(org.jboss.test.helper.ClientHelper.class)
               .addClass(org.jboss.test.helper.TestServlet.class)
               .addClass(org.jboss.test.ws.jaxws.clientConfig.CustomHandler.class)
               .addClass(org.jboss.test.ws.jaxws.clientConfig.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.clientConfig.Helper.class)
               .addClass(org.jboss.test.ws.jaxws.clientConfig.LogHandler.class)
               .addClass(org.jboss.test.ws.jaxws.clientConfig.RoutingHandler.class)
               .addClass(org.jboss.test.ws.jaxws.clientConfig.TestUtils.class)
               .addClass(org.jboss.test.ws.jaxws.clientConfig.UserHandler.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/clientConfig/META-INF/permissions.xml"), "permissions.xml");
         }
      });
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-clientConfig.war") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.clientConfig.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.clientConfig.EndpointImpl.class);
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSTestSetup(ClientConfigurationTestCaseForked.class, JBossWSTestHelper.writeToFile(createDeployments()));
   }
   
   /**
    * Verifies the default client configuration from AS model is used
    * 
    * @throws Exception
    */
   public void testDefaultClientConfigurationInContainer() throws Exception {
      assertEquals("1", runTestInContainer("testDefaultClientConfiguration"));
   }
   
   public void testDefaultClientConfigurationOnDispatchInContainer() throws Exception {
      assertEquals("1", runTestInContainer("testDefaultClientConfigurationOnDispatch"));
   }
   
   /**
    * Verifies a client configuration from AS model can be set
    * 
    * @throws Exception
    */
   public void testCustomClientConfigurationInContainer() throws Exception {
      assertEquals("1", runTestInContainer("testCustomClientConfiguration"));
   }
   
   public void testCustomClientConfigurationOnDispatchInContainer() throws Exception {
      assertEquals("1", runTestInContainer("testCustomClientConfigurationOnDispatch"));
   }
   
   public void testCustomClientConfigurationUsingFeatureInContainer() throws Exception {
      assertEquals("1", runTestInContainer("testCustomClientConfigurationUsingFeature"));
   }
   
   public void testCustomClientConfigurationOnDispatchUsingFeatureInContainer() throws Exception {
      assertEquals("1", runTestInContainer("testCustomClientConfigurationOnDispatchUsingFeature"));
   }
   
   // -------------------------
   
   private String runTestInContainer(String test) throws Exception
   {
      URL url = new URL("http://" + getServerHost()
            + ":8080/jaxws-clientConfig-inContainer-client?path=/jaxws-clientConfig/EndpointImpl&method=" + test
            + "&helper=" + Helper.class.getName());
      return IOUtils.readAndCloseStream(url.openStream());
   }
}
