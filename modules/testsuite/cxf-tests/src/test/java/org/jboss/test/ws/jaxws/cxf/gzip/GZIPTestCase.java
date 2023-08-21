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
package org.jboss.test.ws.jaxws.cxf.gzip;

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
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * 
 * @author alessio.soldano@jboss.com
 * @since 15-Sep-2010
 *
 */
@RunWith(Arquillian.class)
public class GZIPTestCase extends JBossWSTest
{
   private static final String DEP = "jaxws-cxf-gzip";
   private static final String CLIENT_DEP = "jaxws-cxf-gzip-client";
   
   @ArquillianResource
   private URL baseURL;
   
   private Helper helper;
   
   @Deployment(name = DEP, testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, DEP + ".war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.apache.cxf\n"))
            .addClass(org.jboss.test.ws.jaxws.cxf.gzip.HelloWorld.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.gzip.HelloWorldImpl.class)
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/gzip/WEB-INF/web.xml"));
      return archive;
   }

   @Deployment(name = CLIENT_DEP, testable = false)
   public static WebArchive createClientDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, CLIENT_DEP + ".war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client services,org.apache.cxf.impl\n")) //cxf impl dependency because of classes used in the test Helper
            .addClass(org.jboss.test.ws.jaxws.cxf.gzip.GZIPEnforcingInInterceptor.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.gzip.HelloWorld.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.gzip.Helper.class)
            .addClass(org.jboss.wsf.test.ClientHelper.class)
            .addClass(org.jboss.wsf.test.TestServlet.class)
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/gzip/WEB-INF/permissions.xml"), "permissions.xml");
      return archive;
   }
   
   @Test
   @RunAsClient
   @OperateOnDeployment(CLIENT_DEP)
   public void testInContainerGZIPUsingFeatureOnBus() throws Exception
   {
      assertEquals("1", runTestInContainer("testGZIPUsingFeatureOnBus"));
   }
   
   @Test
   @RunAsClient
   @OperateOnDeployment(CLIENT_DEP)
   public void testInContainerGZIPUsingFeatureOnClient() throws Exception
   {
      assertEquals("1", runTestInContainer("testGZIPUsingFeatureOnClient"));
   }
   
   @Test
   @RunAsClient
   @OperateOnDeployment(CLIENT_DEP)
   public void testInContainerGZIPServerSideOnlyInterceptorOnClient() throws Exception
   {
      assertEquals("1", runTestInContainer("testGZIPServerSideOnlyInterceptorOnClient"));
   }
   
   @Test
   @RunAsClient
   @OperateOnDeployment(CLIENT_DEP)
   public void testInContainerFailureGZIPServerSideOnlyInterceptorOnClient() throws Exception
   {
      assertEquals("1", runTestInContainer("testFailureGZIPServerSideOnlyInterceptorOnClient"));
   }
   
   @Test
   @RunAsClient
   @OperateOnDeployment(CLIENT_DEP)
   public void testInContainerGZIPServerSideOnlyInterceptorsOnBus() throws Exception
   {
      assertEquals("1", runTestInContainer("testGZIPServerSideOnlyInterceptorsOnBus"));
   }

   @Test
   @RunAsClient
   @OperateOnDeployment(CLIENT_DEP)
   public void testInContainerFailureGZIPServerSideOnlyInterceptorsOnBus() throws Exception
   {
      assertEquals("1", runTestInContainer("testFailureGZIPServerSideOnlyInterceptorsOnBus"));
   }
   
   private String runTestInContainer(String test) throws Exception
   {
      URL url = new URL(baseURL + "?path=/jaxws-cxf-gzip/HelloWorldService/HelloWorldImpl&method=" + test
            + "&helper=" + Helper.class.getName());
      return IOUtils.readAndCloseStream(url.openStream());
   }
   
   private Helper getHelper()
   {
      if (helper == null)
      {
         helper = new Helper(baseURL + "HelloWorldService/HelloWorldImpl");
      }
      return helper;
   }
   
   @Test
   @RunAsClient
   @OperateOnDeployment(DEP)
   public void testGZIPUsingFeatureOnBus() throws Exception
   {
      assertTrue(getHelper().testGZIPUsingFeatureOnBus());
   }
   
   @Test
   @RunAsClient
   @OperateOnDeployment(DEP)
   public void testGZIPUsingFeatureOnClient() throws Exception
   {
      assertTrue(getHelper().testGZIPUsingFeatureOnClient());
   }
   
   @Test
   @RunAsClient
   @OperateOnDeployment(DEP)
   public void testGZIPServerSideOnlyInterceptorOnClient() throws Exception
   {
      assertTrue(getHelper().testGZIPServerSideOnlyInterceptorOnClient());
   }
   
   @Test
   @RunAsClient
   @OperateOnDeployment(DEP)
   public void testFailureGZIPServerSideOnlyInterceptorOnClient() throws Exception
   {
      assertTrue(getHelper().testFailureGZIPServerSideOnlyInterceptorOnClient());
   }
   
   @Test
   @RunAsClient
   @OperateOnDeployment(DEP)
   public void testGZIPServerSideOnlyInterceptorsOnBus() throws Exception
   {
      assertTrue(getHelper().testGZIPServerSideOnlyInterceptorsOnBus());
   }

   @Test
   @RunAsClient
   @OperateOnDeployment(DEP)
   public void testFailureGZIPServerSideOnlyInterceptorsOnBus() throws Exception
   {
      assertTrue(getHelper().testFailureGZIPServerSideOnlyInterceptorsOnBus());
   }
}
