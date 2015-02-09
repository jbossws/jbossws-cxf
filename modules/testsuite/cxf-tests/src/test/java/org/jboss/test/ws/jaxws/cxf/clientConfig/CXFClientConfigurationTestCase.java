/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.clientConfig;

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
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.WrapThreadContextClassLoader;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Verifies client configuration setup
 *
 * @author alessio.soldano@jboss.com
 * @since 31-May-2012
 */
@RunWith(Arquillian.class)
public class CXFClientConfigurationTestCase extends JBossWSTest
{
   private static final String DEP = "jaxws-cxf-clientConfig";
   private static final String CLIENT_DEP = "jaxws-cxf-clientConfig-inContainer-client";
   
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(name = DEP, testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, DEP + ".war");
      archive.addManifest()
            .addClass(org.jboss.test.ws.jaxws.cxf.clientConfig.Endpoint.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.clientConfig.EndpointImpl.class);
      return archive;
   }

   @Deployment(name = CLIENT_DEP, testable = false)
   public static WebArchive createDeployment2() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, CLIENT_DEP + ".war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.apache.cxf.impl\n"))
            .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/clientConfig/META-INF/jaxws-client-config.xml"), "META-INF/jaxws-client-config.xml")
            .addClass(org.jboss.test.ws.jaxws.cxf.clientConfig.Endpoint.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.clientConfig.Helper.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.clientConfig.TestUtils.class)
            .addClass(org.jboss.wsf.test.ClientHelper.class)
            .addClass(org.jboss.wsf.test.TestServlet.class)
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/clientConfig/META-INF/permissions.xml"), "permissions.xml");
      return archive;
   }
   
   @Override
   protected String getClientJarPaths() {
      return JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-cxf-clientConfig-client.jar") { {
         archive
               .addManifest()
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/clientConfig/META-INF/jaxws-client-config.xml"), "jaxws-client-config.xml");
         }
      });
   }

   /**
    * Verifies a custom client configuration can be read from conf file and set
    * 
    * @throws Exception
    */
   @Test
   @RunAsClient
   @OperateOnDeployment(DEP)
   @WrapThreadContextClassLoader
   public void testCustomClientConfigurationFromFile() throws Exception {
      assertTrue(getHelper().testCustomClientConfigurationFromFile());
   }

   @Test
   @RunAsClient
   @OperateOnDeployment(DEP)
   @WrapThreadContextClassLoader
   public void testCustomClientConfigurationOnDispatchFromFile() throws Exception {
      assertTrue(getHelper().testCustomClientConfigurationOnDispatchFromFile());
   }

   @Test
   @RunAsClient
   @OperateOnDeployment(CLIENT_DEP)
   public void testCustomClientConfigurationFromFileInContainer() throws Exception {
      assertEquals("1", runTestInContainer("testCustomClientConfigurationFromFile"));
   }
   
   @Test
   @RunAsClient
   @OperateOnDeployment(CLIENT_DEP)
   public void testCustomClientConfigurationOnDispatchFromFileInContainer() throws Exception {
      assertEquals("1", runTestInContainer("testCustomClientConfigurationOnDispatchFromFile"));
   }
   
   @Test
   @RunAsClient
   @OperateOnDeployment(DEP)
   @WrapThreadContextClassLoader
   public void testCustomClientConfigurationFromFileUsingFeature() throws Exception {
      assertTrue(getHelper().testCustomClientConfigurationFromFileUsingFeature());
   }

   @Test
   @RunAsClient
   @OperateOnDeployment(DEP)
   @WrapThreadContextClassLoader
   public void testCustomClientConfigurationOnDispatchFromFileUsingFeature() throws Exception {
      assertTrue(getHelper().testCustomClientConfigurationOnDispatchFromFileUsingFeature());
   }

   @Test
   @RunAsClient
   @OperateOnDeployment(CLIENT_DEP)
   public void testCustomClientConfigurationFromFileUsingFeatureInContainer() throws Exception {
      assertEquals("1", runTestInContainer("testCustomClientConfigurationFromFileUsingFeature"));
   }
   
   @Test
   @RunAsClient
   @OperateOnDeployment(CLIENT_DEP)
   public void testCustomClientConfigurationOnDispatchFromFileUsingFeatureInContainer() throws Exception {
      assertEquals("1", runTestInContainer("testCustomClientConfigurationOnDispatchFromFileUsingFeature"));
   }
   
   /**
    * Verifies a client configuration can be changed after another one has been set
    * 
    * @throws Exception
    */
   @Test
   @RunAsClient
   @OperateOnDeployment(DEP)
   @WrapThreadContextClassLoader
   public void testConfigurationChange() throws Exception {
      assertTrue(getHelper().testConfigurationChange());
   }

   @Test
   @RunAsClient
   @OperateOnDeployment(DEP)
   @WrapThreadContextClassLoader
   public void testConfigurationChangeOnDispatch() throws Exception {
      assertTrue(getHelper().testConfigurationChangeOnDispatch());
   }

   @Test
   @RunAsClient
   @OperateOnDeployment(CLIENT_DEP)
   public void testConfigurationChangeInContainer() throws Exception {
      assertEquals("1", runTestInContainer("testConfigurationChange"));
   }

   @Test
   @RunAsClient
   @OperateOnDeployment(CLIENT_DEP)
   public void testConfigurationChangeOnDispatchInContainer() throws Exception {
      assertEquals("1", runTestInContainer("testConfigurationChangeOnDispatch"));
   }
   
   // -------------------------
   
   private Helper getHelper() {
      Helper helper = new Helper();
      helper.setTargetEndpoint(baseURL + "EndpointImpl");
      return helper;
   }
   
   private String runTestInContainer(String test) throws Exception
   {
      URL url = new URL(baseURL + "?path=/jaxws-cxf-clientConfig/EndpointImpl&method=" + test + "&helper=" + Helper.class.getName());
      return IOUtils.readAndCloseStream(url.openStream());
   }
}
