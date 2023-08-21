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
package org.jboss.test.ws.jaxws.jbws2074.usecase5.client;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.ws.jaxws.jbws2074.usecase5.service.EJB3Iface;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-2074] Resource injection in jaxws endpoints and handlers
 * [JBWS-3846] Refactor creation process of jaxws handlers from predefined configurations
 *
 * @author ropalka@redhat.com
 * @author alessio.soldano@jboss.com
 * 
 * @since 03-Mar-2015
 */
@RunWith(Arquillian.class)
public final class JBWS2074TestCase extends JBossWSTest
{
   private static final String JAR_DEPLOYMENT = "jaxws-jbws2074-usecase5";
   private static final String EAR_DEPLOYMENT = "jaxws-jbws2074-ear-usecase5";

   @ArquillianResource
   Deployer deployer;

   private static JavaArchive getJarArchive() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, JAR_DEPLOYMENT + ".jar");
         archive
            .setManifest(new StringAsset("Manifest-Version: 1.0\n"
               + "Dependencies: org.jboss.logging\n"))
            .addClass(org.jboss.test.ws.jaxws.jbws2074.handler.DescriptorResourcesHandler.class)
            .addClass(org.jboss.test.ws.jaxws.jbws2074.handler.JavaResourcesHandler.class)
            .addClass(org.jboss.test.ws.jaxws.jbws2074.handler.ManualResourcesHandler.class)
            .addClass(org.jboss.test.ws.jaxws.jbws2074.usecase5.service.EJB3Iface.class)
            .addClass(org.jboss.test.ws.jaxws.jbws2074.usecase5.service.EJB3Impl.class)
            .addAsResource("org/jboss/test/ws/jaxws/jbws2074/usecase5/service/endpoint-config.xml", "endpoint-config.xml")
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2074/usecase5/META-INF/ejb-jar.xml"), "ejb-jar.xml");
      return archive;
   }

   @Deployment(name = JAR_DEPLOYMENT, testable = false, managed = false, order = 1)
   public static JavaArchive createClientDeployment1() {
      return getJarArchive();
   }

   @Deployment(name = EAR_DEPLOYMENT, testable = false, managed = false, order = 2)
   public static EnterpriseArchive createClientDeployment() {
      EnterpriseArchive archive = ShrinkWrap.create(EnterpriseArchive.class, "jaxws-jbws2074-usecase5.ear");
         archive
            .addManifest()
            .addAsModule(getJarArchive())
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2074/usecase5-ear/META-INF/application.xml"), "application.xml");
      return archive;
   }

   public void executeTest() throws Exception
   {
      String endpointAddress = "http://" + getServerHost() + ":" + getServerPort() + "/jaxws-jbws2074-usecase5/Service";
      QName serviceName = new QName("http://ws.jboss.org/jbws2074", "EJB3Service");
      Service service = Service.create(new URL(endpointAddress + "?wsdl"), serviceName);
      EJB3Iface port = (EJB3Iface)service.getPort(EJB3Iface.class);

      String retStr = port.echo("hello");

      StringBuffer expStr = new StringBuffer("hello");
      expStr.append(":Inbound:ManualResourcesHandler");
      expStr.append(":Inbound:JavaResourcesHandler");
      expStr.append(":Inbound:DescriptorResourcesHandler");
      expStr.append(":EJB3Impl");
      expStr.append(":Outbound:DescriptorResourcesHandler");
      expStr.append(":Outbound:JavaResourcesHandler");
      expStr.append(":Outbound:ManualResourcesHandler");
      assertEquals(expStr.toString(), retStr);
   }

   @Test
   @RunAsClient
   public void testUsecase5WithoutEar() throws Exception
   {
      try
      {
         deployer.deploy(JAR_DEPLOYMENT);
         executeTest();
      }
      finally
      {
         deployer.undeploy(JAR_DEPLOYMENT);
      }
   }

   @Test
   @RunAsClient
   public void testUsecase5WithEar() throws Exception
   {
      try
      {
         deployer.deploy(EAR_DEPLOYMENT);
         executeTest();
      }
      finally
      {
         deployer.undeploy(EAR_DEPLOYMENT);
      }
   }

}
