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
package org.jboss.test.ws.jaxws.samples.webservicerefsec;

import java.io.File;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test multiple webserviceref fro the same endpoint with different security credentials
 *
 * @author alessio.soldano@jboss.com
 * @since 12-May-2010
 */
@RunWith(Arquillian.class)
public class WebServiceRefSecTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(name="jaxws-samples-webservicerefsec", order=1, testable = false)
   public static JavaArchive createDeployment() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-samples-webservicerefsec.jar");
      archive
         .addManifest()
         .addClass(org.jboss.test.ws.jaxws.samples.webservicerefsec.EndpointImpl.class);
      return archive;
   }

   @Deployment(name="jaxws-samples-webservicerefsec-servlet-client", order=2, testable = false)
   public static WebArchive createDeployment1() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-webservicerefsec-servlet-client.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.samples.webservicerefsec.Client.class)
               .addClass(org.jboss.test.ws.jaxws.samples.webservicerefsec.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.samples.webservicerefsec.EndpointService.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/webservicerefsec/WEB-INF/jboss-web.xml"), "jboss-web.xml")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/webservicerefsec/WEB-INF/wsdl/Endpoint.wsdl"), "wsdl/Endpoint.wsdl")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/webservicerefsec/WEB-INF/permissions.xml"), "permissions.xml")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/webservicerefsec/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-samples-webservicerefsec-servlet-client")
   public void testServletClient() throws Exception
   {
      URL url = new URL("http://" + baseURL.getHost() + ":" + baseURL.getPort() + "/jaxws-samples-webservicerefsec-servlet-client?echo=HelloWorld");
      assertEquals("HelloWorld", IOUtils.readAndCloseStream(url.openStream()));
   }
}
