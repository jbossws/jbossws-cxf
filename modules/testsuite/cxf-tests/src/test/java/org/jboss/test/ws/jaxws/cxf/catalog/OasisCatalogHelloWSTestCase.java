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
package org.jboss.test.ws.jaxws.cxf.catalog;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.catalog.OASISCatalogManager;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * User: rsearls
 * Date: 7/9/14
 */
@RunWith(Arquillian.class)
public class OasisCatalogHelloWSTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-cxf-catalog.war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
            + "Dependencies: org.apache.cxf\n"))
         .addClass(org.jboss.test.ws.jaxws.cxf.catalog.HelloRequest.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.catalog.HelloResponse.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.catalog.HelloWsImpl.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.catalog.HelloWs.class)
         .add(new FileAsset(new File(JBossWSTestHelper.getTestResourcesDir() +
            "/jaxws/cxf/catalog/META-INF/jax-ws-catalog.xml")), "META-INF/jax-ws-catalog.xml")
            // stnd file locations required for successful deployment
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir()
            + "/jaxws/cxf/catalog/META-INF/wsdl/HelloService.wsdl"), "wsdl/HelloService.wsdl")
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir()
            + "/jaxws/cxf/catalog/META-INF/wsdl/Hello_schema1.xsd"), "wsdl/Hello_schema1.xsd")
            // sever side catalog maps to these files.
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir()
            + "/jaxws/cxf/catalog/META-INF/wsdl/HelloService.wsdl"), "wsdl/foo/HelloService.wsdl")
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir()
            + "/jaxws/cxf/catalog/META-INF/wsdl/Hello_schema1.xsd"), "wsdl/foo/Hello_schema1.xsd");
      JBossWSTestHelper.writeToFile(archive);
      return archive;
   }

   @Test
   @RunAsClient
   public void testCatalogOnClientSide() throws Exception
   {
      Bus bus = BusFactory.newInstance().createBus();
      try {
         BusFactory.setThreadDefaultBus(bus);

         URL archiveURL =  JBossWSTestHelper.getArchiveURL("jaxws-cxf-catalog.war");

         // add archive to classpath
         ClassLoader currentThreadClassLoader = Thread.currentThread().getContextClassLoader();
         URLClassLoader urlClassLoader
            = new URLClassLoader(new URL[]{archiveURL}, currentThreadClassLoader);
         Thread.currentThread().setContextClassLoader(urlClassLoader);

         QName serviceName = new QName(
            org.jboss.test.ws.jaxws.cxf.catalog.HelloWs.TARGET_NAMESPACE,
            org.jboss.test.ws.jaxws.cxf.catalog.HelloWs.NAME);
         URL wsdlURL = new URL(baseURL + "HelloService?wsdl");
         Service service = Service.create(wsdlURL, serviceName);

         OASISCatalogManager catalogManager = bus.getExtension(OASISCatalogManager.class);
         assertNotNull("OASISCatalogManager not provided ", catalogManager);

         String xsd = "http://org.jboss.ws/cxf/catalogclient/ws-addr.xsd";
         String resolvedSchemaLocation = catalogManager.resolveSystem(xsd);
         assertEquals("http://org.foo.bar/client/ws-addr.xsd", resolvedSchemaLocation);

      } finally {
         bus.shutdown(true);
      }
   }

   @Test
   @RunAsClient
   public void testCatalogInJbosswsCxfClientJar() throws Exception
   {
      Bus bus = BusFactory.newInstance().createBus();
      try {
         BusFactory.setThreadDefaultBus(bus);

         QName serviceName = new QName(
            org.jboss.test.ws.jaxws.cxf.catalog.HelloWs.TARGET_NAMESPACE,
            org.jboss.test.ws.jaxws.cxf.catalog.HelloWs.NAME);
         URL wsdlURL = new URL(baseURL + "HelloService?wsdl");
         Service service = Service.create(wsdlURL, serviceName);

         // jbossws-cxf-client.Jar is on the classpath by default.
         // cxf processed it during service creation.
         OASISCatalogManager catalogManager = bus.getExtension(OASISCatalogManager.class);
         assertNotNull("OASISCatalogManager not provided ", catalogManager);

         String xsd = "http://ws-i.org/profiles/basic/1.1/ws-addr.xsd";
         String resolvedSchemaLocation = catalogManager.resolveSystem(xsd);
         assertEquals("classpath:/schemas/wsdl/ws-addr.xsd", resolvedSchemaLocation);

      } finally {
         bus.shutdown(true);
      }
   }


   @Test
   @RunAsClient
   public void testCatalogOnServerSide() throws Exception
   {
      Bus bus = BusFactory.newInstance().createBus();
      try {
         BusFactory.setThreadDefaultBus(bus);

         QName serviceName = new QName(
            org.jboss.test.ws.jaxws.cxf.catalog.HelloWs.TARGET_NAMESPACE,
            org.jboss.test.ws.jaxws.cxf.catalog.HelloWs.NAME);
         URL wsdlURL = new URL(baseURL + "HelloService?wsdl");
         Service service = Service.create(wsdlURL, serviceName);
         HelloWs proxy = service.getPort(HelloWs.class);
         HelloRequest helloReq = new HelloRequest();
         helloReq.setInput("Anyone home?");
         proxy.doHello(helloReq);

      } finally {
         bus.shutdown(true);
      }
   }
}
