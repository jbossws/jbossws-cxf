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
package org.jboss.test.ws.jaxws.samples.addressing;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.soap.AddressingFeature;

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
 * Test stateful endpoint using ws-addressing
 *
 * @author Thomas.Diesler@jboss.org
 * @author alessio.soldano@jboss.com
 * @since 24-Nov-2005
 */
@RunWith(Arquillian.class)
public class AddressingStatefulTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-wsaddressing.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.samples.addressing.ServerHandler.class)
               .addClass(org.jboss.test.ws.jaxws.samples.addressing.StatefulEndpoint.class)
               .addClass(org.jboss.test.ws.jaxws.samples.addressing.StatefulEndpointImpl.class)
               .addAsResource("org/jboss/test/ws/jaxws/samples/addressing/jaxws-handlers.xml")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/addressing/WEB-INF/permissions.xml"), "permissions.xml")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/addressing/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testItemLifecycle() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/TestService?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/samples/wsaddressing", "TestService");

      Service service1 = Service.create(wsdlURL, serviceName);
      AddressingPort port1 = new AddressingPort(service1.getPort(StatefulEndpoint.class, new AddressingFeature(true, true)));

      Service service2 = Service.create(wsdlURL, serviceName);
      AddressingPort port2 = new AddressingPort(service2.getPort(StatefulEndpoint.class, new AddressingFeature(true, true)));
      
      port1.addItem("Ice Cream");
      port1.addItem("Ferrari");

      port2.addItem("Mars Bar");
      port2.addItem("Porsche");
      
      String items1 = port1.getItems();
      assertEquals("[Ice Cream, Ferrari]", items1);

      String items2 = port2.getItems();
      assertEquals("[Mars Bar, Porsche]", items2);
      
      port1.checkout();
      assertEquals("[]", port1.getItems());

      port2.checkout();
      assertEquals("[]", port2.getItems());
   }
}
