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
package org.jboss.test.ws.publish;

import java.io.File;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
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
 * Test WS endpoint publish api to JBoss AS container
 *
 * @author alessio.soldano@jboss.com
 * @since 13-Jul-2011
 */
@RunWith(Arquillian.class)
public class EndpointPublishTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class,"endpoint-publish.war");
         archive
            .setManifest(new StringAsset("Manifest-Version: 1.0\n"
               + "Dependencies: org.jboss.ws.common,org.jboss.as.server \n"))
            .addAsResource("org/jboss/test/ws/publish/WEB-INF/wsdl/EndpointImpl3.xml", "WEB-INF/wsdl/EndpointImpl3.xml")
            .addAsResource("org/jboss/test/ws/publish/WEB-INF/wsdl/EndpointImpl4.xml", "WEB-INF/wsdl/EndpointImpl4.xml")
            .addClass(org.jboss.test.ws.publish.Endpoint.class)
            .addClass(org.jboss.test.ws.publish.EndpointImpl.class)
            .addClass(org.jboss.test.ws.publish.EndpointImpl2.class)
            .addClass(org.jboss.test.ws.publish.EndpointImpl3.class)
            .addClass(org.jboss.test.ws.publish.EndpointImpl4.class)
            .addClass(org.jboss.test.ws.publish.EndpointImpl5.class)
            .addAsResource("org/jboss/test/ws/publish/EndpointImpl5.xml")
            .addClass(org.jboss.test.ws.publish.EndpointPublishServlet.class)
            .addAsResource("org/jboss/test/ws/publish/TestService.xml")
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/publish/META-INF/permissions.xml"), "permissions.xml")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/publish/WEB-INF/wsdl/EndpointImpl3.xml"), "wsdl/EndpointImpl3.xml");
      return archive;
   }

   @Test
   @RunAsClient
   public void testEndpointPublish() throws Exception
   {
      URL url = new URL(baseURL + "/endpoint-publish");
      assertEquals("1", IOUtils.readAndCloseStream(url.openStream()));
   }
}
