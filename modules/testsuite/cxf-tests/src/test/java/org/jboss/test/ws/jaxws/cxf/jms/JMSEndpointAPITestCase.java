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
package org.jboss.test.ws.jaxws.cxf.jms;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.net.URL;

/**
 * Test case for publishing a JMS (SOAP-over-JMS 1.0) endpoint through API 
 *
 * @author alessio.soldano@jboss.com
 * @since 29-Apr-2011
 */
@RunWith(Arquillian.class)
public final class JMSEndpointAPITestCase extends JBossWSTest
{
   private static final String JMS_SERVER = "jms";
   
   private static boolean useHornetQ() {
      return JBossWSTestHelper.isTargetWildFly9();
   }

   @Deployment(testable = false)
   @TargetsContainer(JMS_SERVER)
   public static WebArchive createWarDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class,"jaxws-cxf-jms-api.war");
         archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client services," + (useHornetQ() ? "org.hornetq\n" : "org.apache.activemq.artemis")))
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jms/META-INF/permissions.xml"), "permissions.xml")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jms/META-INF/wsdl/HelloWorldService.wsdl"), "classes/META-INF/wsdl/HelloWorldService.wsdl")
               .addClass(org.jboss.test.ws.jaxws.cxf.jms.HelloWorld.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.jms.HelloWorldImpl.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.jms.TestServlet.class);
      return archive;
   }

   @Test
   @RunAsClient
   public void testServerSide() throws Exception
   {
      URL url = new URL("http://" + getServerHost() + ":" + getServerPort(CXF_TESTS_GROUP_QUALIFIER, JMS_SERVER) + "/jaxws-cxf-jms-api");
      assertEquals("true", IOUtils.readAndCloseStream(url.openStream()));
   }
   
}
