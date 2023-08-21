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
package org.jboss.test.ws.jaxws.jbws1581;

import java.io.File;
import java.net.URL;

import javax.naming.InitialContext;
import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * EJB vehicle using loader repository not sufficiently isolated
 *
 * http://jira.jboss.org/jira/browse/JBWS-1581
 *
 * @author Thomas.Diesler@jboss.com
 * @since 19-Mar-2007
 */
@RunWith(Arquillian.class)
public class JBWS1581TestCase extends JBossWSTest
{
   private static final String WAR_DEPLOYMENT = "jaxws-jbws1581-pojo";
   private static final String EAR_DEPLOYMENT = "jaxws-jbws1581";

   @ArquillianResource
   private URL baseURL;

   @ArquillianResource
   Deployer deployer;

   @Deployment(name = WAR_DEPLOYMENT, order = 1, testable = false, managed = false)
   public static WebArchive createDeployment1() {
      return createWarDeployment();
   }

   @Deployment(name="jaxws-jbws1581-ejb3", order=2, testable = false)
   public static JavaArchive createDeployment2() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws1581-ejb3.jar");
         archive
            .addManifest()
            .addClass(org.jboss.test.ws.jaxws.jbws1581.EJB3Bean.class)
            .addClass(org.jboss.test.ws.jaxws.jbws1581.EJB3Remote.class)
            .addClass(org.jboss.test.ws.jaxws.jbws1581.EndpointInterface.class)
            .addClass(org.jboss.test.ws.jaxws.jbws1581.EndpointService.class)
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1581/META-INF/permissions.xml"), "permissions.xml")
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1581/META-INF/wsdl/TestService.wsdl"), "wsdl/TestService.wsdl");
      return archive;
   }

   @Deployment(name = EAR_DEPLOYMENT, order = 3, testable = false, managed = false)
   public static EnterpriseArchive createDeployment3() {
      EnterpriseArchive archive = ShrinkWrap.create(EnterpriseArchive.class, EAR_DEPLOYMENT + ".ear");
         archive
            .addManifest()
            .addAsModule(createWarDeployment());
      return archive;
   }
   
   private static WebArchive createWarDeployment() {
	  WebArchive archive = ShrinkWrap.create(WebArchive.class, WAR_DEPLOYMENT + ".war");
	     archive
	        .addManifest()
	        .addClass(org.jboss.test.ws.jaxws.jbws1581.EndpointBean.class)
	        .addClass(org.jboss.test.ws.jaxws.jbws1581.EndpointInterface.class)
	        .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1581/WEB-INF/web.xml"));
	  return archive;
   }
   
   @Test
   @RunAsClient
   public void testWSDLAccessWar() throws Exception {
      try {
         deployer.deploy(WAR_DEPLOYMENT);
         internalTestWSDLAccess();
      } finally {
         deployer.undeploy(WAR_DEPLOYMENT);
      }
   }

   @Test
   @RunAsClient
   public void testEJBVehicleWar() throws Exception {
      try {
         deployer.deploy(WAR_DEPLOYMENT);
         internalTestEJBVehicle();
      } finally {
         deployer.undeploy(WAR_DEPLOYMENT);
      }
   }

   @Test
   @RunAsClient
   public void testWSDLAccessEar() throws Exception {
      try {
         deployer.deploy(EAR_DEPLOYMENT);
         internalTestWSDLAccess();
      } finally {
         deployer.undeploy(EAR_DEPLOYMENT);
      }
   }

   @Test
   @RunAsClient
   public void testEJBVehicleEar() throws Exception {
      try {
         deployer.deploy(EAR_DEPLOYMENT);
         internalTestEJBVehicle();
      } finally {
         deployer.undeploy(EAR_DEPLOYMENT);
      }
   }

   private void internalTestWSDLAccess() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/jaxws-jbws1581-pojo?wsdl");
      Definition wsdl = WSDLFactory.newInstance().newWSDLReader().readWSDL(wsdlURL.toString());
      assertNotNull("wsdl expected", wsdl);
   }

   private void internalTestEJBVehicle() throws Exception
   {
      InitialContext iniCtx = null;
      try
      {
         iniCtx = getServerInitialContext();
         EJB3Remote remote = (EJB3Remote)iniCtx.lookup("jaxws-jbws1581-ejb3//EJB3Bean!" + EJB3Remote.class.getName());
         String retStr = remote.runTest("Hello World!");
         assertEquals("Hello World!", retStr);
      }
      finally
      {
         if (iniCtx != null)
         {
            iniCtx.close();
         }
      }
   }
}
