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
package org.jboss.test.ws.jaxws.jbws2074.usecase3.client;

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
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.ws.jaxws.jbws2074.usecase3.service.POJOIface;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-2074] Resource injection in jaxws endpoints and handlers
 *
 * @author ropalka@redhat.com
 */
@RunWith(Arquillian.class)
public final class JBWS2074TestCase extends JBossWSTest
{
   private static final String WAR_DEPLOYMENT = "jaxws-jbws2074-usecase3";
   private static final String EAR_DEPLOYMENT = "jaxws-jbws2074-ear-usecase3";

   @ArquillianResource
   Deployer deployer;

   private static WebArchive getWarArchive() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, WAR_DEPLOYMENT + ".war");
         archive
            .setManifest(new StringAsset("Manifest-Version: 1.0\n"
               + "Dependencies: org.jboss.logging\n"))
            .addClass(org.jboss.test.ws.jaxws.jbws2074.usecase3.service.POJOIface.class)
            .addClass(org.jboss.test.ws.jaxws.jbws2074.usecase3.service.POJOImpl.class)
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2074/usecase3/WEB-INF/web.xml"));
      return archive;
   }

   @Deployment(name = WAR_DEPLOYMENT, testable = false, managed = false, order = 1)
   public static WebArchive createClientDeployment1() {
      return getWarArchive();
   }

   @Deployment(name = EAR_DEPLOYMENT, testable = false, managed = false, order = 2)
   public static EnterpriseArchive createClientDeployment() {
      EnterpriseArchive archive = ShrinkWrap.create(EnterpriseArchive.class, EAR_DEPLOYMENT + ".ear");
         archive
            .addManifest()
            .addAsModule(getWarArchive())
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2074/usecase3-ear/META-INF/application.xml"), "application.xml");
      return archive;
   }

   public void executeTest() throws Exception
   {
      String endpointAddress = "http://" + getServerHost() + ":" + getServerPort() + "/jaxws-jbws2074-usecase3/Service";
      QName serviceName = new QName("http://ws.jboss.org/jbws2074", "POJOService");
      Service service = Service.create(new URL(endpointAddress + "?wsdl"), serviceName);
      POJOIface port = (POJOIface)service.getPort(POJOIface.class);

      String retStr = port.echo("hello");

      StringBuffer expStr = new StringBuffer("hello");
      expStr.append(":POJOImpl");
      assertEquals(expStr.toString(), retStr);
   }

   @Test
   @RunAsClient
   public void testUsecase3WithoutEar() throws Exception
   {
      try
      {
         deployer.deploy(WAR_DEPLOYMENT);
         executeTest();
      }
      finally
      {
         deployer.undeploy(WAR_DEPLOYMENT);
      }
   }

   @Test
   @RunAsClient
   public void testUsecase3WithEar() throws Exception
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
