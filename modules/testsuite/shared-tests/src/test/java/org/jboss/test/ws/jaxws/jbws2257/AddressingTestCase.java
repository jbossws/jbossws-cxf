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
package org.jboss.test.ws.jaxws.jbws2257;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author alessio.soldano@jboss.com
 * @since 25-Nov-2008
 */
@RunWith(Arquillian.class)
public final class AddressingTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static JavaArchive createDeployments() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws2257.jar");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws2257.ServiceIface.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2257.ServiceImpl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2257.jaxws.SayHello.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2257.jaxws.SayHelloResponse.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2257/META-INF/permissions.xml"), "permissions.xml")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2257/META-INF/wsdl/AddressingService.wsdl"), "wsdl/AddressingService.wsdl")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2257/META-INF/wsdl/AddressingService_schema1.xsd"), "wsdl/AddressingService_schema1.xsd");
      return archive;
   }

   @Test
   @RunAsClient
   public void test() throws Exception
   {
      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wsaddressing", "AddressingService");
      URL wsdlURL = new URL(baseURL + "/jaxws-jbws2257/AddressingService/ServiceImpl?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(ServiceIface.class);
      assertEquals("Hello World!", proxy.sayHello());
   }
}
