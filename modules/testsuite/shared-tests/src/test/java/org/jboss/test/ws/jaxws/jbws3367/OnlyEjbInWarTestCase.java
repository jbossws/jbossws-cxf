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

package org.jboss.test.ws.jaxws.jbws3367;

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
 * [JBWS-3367][AS7-1605] jboss-web.xml ignored for web service root
 *
 * This test case tests if only EJB3 endpoints are in war archive.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
@RunWith(Arquillian.class)
public class OnlyEjbInWarTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws3367-usecase2.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws3367.EJB3Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3367.EndpointIface.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3367/WEB-INF-2/jboss-web.xml"), "jboss-web.xml");
      return archive;
   }

   @Test
   @RunAsClient
   public void testEJB3Endpoint() throws Exception
   {
      final QName serviceName = new QName("org.jboss.test.ws.jaxws.jbws3367", "EJB3EndpointService");
      final URL wsdlURL = new URL(baseURL +  "/EJB3Endpoint?wsdl");
      final Service service = Service.create(wsdlURL, serviceName);
      final EndpointIface port = service.getPort(EndpointIface.class);
      final String result = port.echo("hello");
      assertEquals("EJB3 hello", result);
   }

}
