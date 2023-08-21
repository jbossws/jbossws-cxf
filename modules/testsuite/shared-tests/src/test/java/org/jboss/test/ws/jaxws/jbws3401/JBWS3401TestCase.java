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
package org.jboss.test.ws.jaxws.jbws3401;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-3401] Support for EJBs bundled in .war archives referencing schemas.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
@RunWith(Arquillian.class)
public class JBWS3401TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws3401.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws3401.TestEndpoint.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3401.TestEndpointImpl.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3401/WEB-INF/wsdl/TestService.wsdl"), "wsdl/TestService.wsdl")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3401/WEB-INF/wsdl/schema1.xsd"), "wsdl/schema1.xsd")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3401/WEB-INF/wsdl/schema2.xsd"), "wsdl/schema2.xsd")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3401/WEB-INF/wsdl/schema3.xsd"), "wsdl/schema3.xsd")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3401/WEB-INF/wsdl/schema4.xsd"), "wsdl/schema4.xsd")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3401/WEB-INF/wsdl/schema5.xsd"), "wsdl/schema5.xsd");
      return archive;
   }

   private TestEndpoint getPort() throws Exception
   {

      URL wsdlURL = new URL(baseURL + "/TestEndpointService/TestEndpoint?wsdl");
      QName serviceName = new QName("http://org.jboss.test.ws/jbws3401", "TestEndpointService");

      Service service = Service.create(wsdlURL, serviceName);

      return service.getPort(TestEndpoint.class);
   }

   @Test
   @RunAsClient
   public void testCall() throws Exception
   {
      String message = "Hi";
      String response = getPort().echo(message);
      assertEquals(message, response);
   }

}
