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
package org.jboss.test.ws.jaxws.jbws2527;

import java.io.File;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * JBWS-2527 testcase: BeanFactory not initialized or already closed
 * 
 * @author richard.opalka@jboss.com
 */
@RunWith(Arquillian.class)
public class JBWS2527TestCase extends JBossWSTest
{
   @ArquillianResource
   Deployer deployer;

   @Deployment(name="jaxws-jbws2527-service", managed=false, testable = false)
   public static WebArchive createClientDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws2527-service.war");
      archive
         .addManifest()
         .addClass(org.jboss.test.ws.jaxws.jbws2527.Hello.class)
         .addClass(org.jboss.test.ws.jaxws.jbws2527.HelloImpl.class)
         .addClass(org.jboss.test.ws.jaxws.jbws2527.HelloService.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2527/WEB-INF-service/jboss-web.xml"), "jboss-web.xml")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2527/WEB-INF-service/web.xml"), "web.xml")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2527/WEB-INF-service/wsdl/HelloService.wsdl"), "wsdl/HelloService.wsdl")
         .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2527/WEB-INF-service/web.xml"));
      return archive;
   }

   @Deployment(name="jaxws-jbws2527-client", managed=false, testable = false)
   public static WebArchive createClientDeployment1() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws2527-client.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws2527.ClientServlet.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2527.Hello.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2527.HelloService.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2527/WEB-INF-client/jboss-web.xml"), "jboss-web.xml")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2527/WEB-INF-client/web.xml"), "web.xml")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2527/WEB-INF-client/wsdl/HelloService.wsdl"), "wsdl/HelloService.wsdl")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2527/WEB-INF-client/permissions.xml"), "permissions.xml")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2527/WEB-INF-client/web.xml"));
      return archive;
   }

  @Test
  @RunAsClient
   public void test() throws Exception
   {
      for (int i = 0; i < 2; i++)
      {
         executeTest();
         executeTest();
      }
   }

   public void executeTest() throws Exception
   {
      try
      {
         deployer.deploy("jaxws-jbws2527-service");
         deployer.deploy("jaxws-jbws2527-client");
         assertEquals("true", IOUtils.readAndCloseStream(new URL("http://" + getServerHost() + ":" + getServerPort() + "/jaxws-jbws2527-client/jbws2527").openStream()));
      }
      finally
      {
         deployer.undeploy("jaxws-jbws2527-client");
         deployer.undeploy("jaxws-jbws2527-service");
      }
   }
}
