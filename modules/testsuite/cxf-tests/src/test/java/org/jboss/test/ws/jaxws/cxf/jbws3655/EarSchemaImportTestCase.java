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
package org.jboss.test.ws.jaxws.cxf.jbws3655;

import java.io.File;
import java.net.URL;

import javax.management.ObjectName;
import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class EarSchemaImportTestCase extends JBossWSTest
{
   @ArquillianResource
   private Deployer deployer;
   private static final String EAR_DEPLOYMENT = "jaxws-cxf-jbws3655.ear";
   private String dataDir;
   private File wsdlDir;

   @Before
   public void setup() throws Exception {
      deployer.deploy(EAR_DEPLOYMENT);
      ObjectName serverEnviroment = new ObjectName("jboss.as:core-service=server-environment");
      dataDir = (String)getServer().getAttribute(serverEnviroment, "dataDir");
      wsdlDir = new File(dataDir+"/wsdl/" + EAR_DEPLOYMENT);
      //JBWS-3992:check wsdl dir is generated
      assertTrue(wsdlDir.getAbsolutePath() + "is expected", wsdlDir.exists());
   }
   @After
   public void cleanup() throws Exception {
      deployer.undeploy(EAR_DEPLOYMENT);
      //JBWS-3992:check wsdl directory is removed
      assertTrue("wsdlDir is expetcted to remove" , !wsdlDir.exists());
   }
   @Deployment(testable = false, name=EAR_DEPLOYMENT,managed=false)
   public static JavaArchive createDeployment3() {
      JavaArchive archive1 = ShrinkWrap.create(JavaArchive.class, "jaxws-cxf-jbws3655-ejb.jar");
      archive1.addManifest().addClass(org.jboss.test.ws.jaxws.cxf.jbws3655.HelloWSEJBImpl.class);
      writeToDisk(archive1);
      
      JavaArchive archive2 = ShrinkWrap.create(JavaArchive.class, "jaxws-cxf-jbws3655-jaxws.jar");
      archive2.addManifest()
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3655.HelloRequest.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3655.HelloResponse.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3655.HelloWs.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3655.HelloWsImpl.class)
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3655/META-INF/wsdl/Hello.wsdl"), "wsdl/Hello.wsdl")
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3655/META-INF/wsdl/Hello_schema1.xsd"), "wsdl/Hello_schema1.xsd");
      writeToDisk(archive2);
      
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-cxf-jbws3655.ear");
      archive.addManifest()
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3655/META-INF/application.xml"))
            .addAsResource(new File(JBossWSTestHelper.getTestArchiveDir(), "jaxws-cxf-jbws3655-jaxws.jar"), "lib/jaxws-cxf-jbws3655-jaxws.jar")
            .addAsResource(new File(JBossWSTestHelper.getTestArchiveDir(), "jaxws-cxf-jbws3655-ejb.jar"));
      return archive;
   }
   
   public static void writeToDisk(JavaArchive archive)
   {
      File file = new File(JBossWSTestHelper.getTestArchiveDir(), archive.getName());
      archive.as(ZipExporter.class).exportTo(file, true);
   }

   @Test
   @RunAsClient
   public void testSchemaImport() throws Exception
   {
      HelloWs port = getPort("http://" + getServerHost() + ":" + getServerPort() + "/jaxws-cxf-jbws3655/HelloService");
      HelloRequest request = new HelloRequest();
      request.setInput("hello");
      HelloResponse response = port.doHello(request);
      assertEquals(2, response.getMultiHello().size());
   }

   private HelloWs getPort(String publishURL) throws Exception
   {
      URL wsdlURL = new URL(publishURL + "?wsdl");
      QName qname = new QName("http://hello/test", "HelloService");
      Service service = Service.create(wsdlURL, qname);
      return (HelloWs)service.getPort(HelloWs.class);
   }
}
