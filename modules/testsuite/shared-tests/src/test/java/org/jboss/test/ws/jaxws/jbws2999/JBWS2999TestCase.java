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
package org.jboss.test.ws.jaxws.jbws2999;

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
 * [JBWS-2999] cxf webservices.xml override with jaxws
 *
 * @author alessio.soldano@jboss.com
 * @since 15-Apr-2010
 */
@RunWith(Arquillian.class)
public class JBWS2999TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static JavaArchive createDeployments() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws2999.jar");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws2999.CustomHandler.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2999.Hello.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2999.HelloBean.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2999/META-INF/ejb-jar.xml"), "ejb-jar.xml")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2999/META-INF/webservices.xml"), "webservices.xml")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2999/META-INF/wsdl/HelloService.wsdl"), "wsdl/HelloService.wsdl");
      return archive;
   }

   private Hello getPort() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/jaxws-jbws2999/JunkServiceName/HelloBean?wsdl");
      QName serviceName = new QName("http://Hello.org", "HelloService");

      Service service = Service.create(wsdlURL, serviceName);
      return service.getPort(Hello.class);
   }

   @Test
   @RunAsClient
   public void testCall() throws Exception
   {
      String message = "Hello";
      String response = getPort().helloEcho(message);
      assertEquals(message + "World", response);
   }
}
