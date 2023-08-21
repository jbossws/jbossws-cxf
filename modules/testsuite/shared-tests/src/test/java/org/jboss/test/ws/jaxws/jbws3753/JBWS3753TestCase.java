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
package org.jboss.test.ws.jaxws.jbws3753;

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
 * [JBWS-3753] Improve destination matching when processing requests
 *
 * @author alessio.soldano@jboss.com
 */
@RunWith(Arquillian.class)
public class JBWS3753TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws3753.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws3753.ServiceAImpl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3753.ServiceBImpl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3753.ServiceImpl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3753.ServiceInterface.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3753/WEB-INF/web.xml"));
     return archive;
   }

   @Test
   @RunAsClient
   public void testService() throws Exception
   {
      Service service = Service.create(new URL(baseURL + "/service?wsdl"), new QName("http://org.jboss.ws/jaxws/jbws3753/", "MyService"));
      ServiceInterface port = service.getPort(ServiceInterface.class);
      assertEquals("Hi John", port.greetMe("John"));
   }

   @Test
   @RunAsClient
   public void testServiceA() throws Exception
   {
      Service service = Service.create(new URL(baseURL + "/serviceA?wsdl"), new QName("http://org.jboss.ws/jaxws/jbws3753/", "MyService"));
      ServiceInterface port = service.getPort(ServiceInterface.class);
      assertEquals("(A) Hi John", port.greetMe("John"));
   }

   @Test
   @RunAsClient
   public void testServiceB() throws Exception
   {
      Service service = Service.create(new URL(baseURL + "/serviceB?wsdl"), new QName("http://org.jboss.ws/jaxws/jbws3753/", "MyService"));
      ServiceInterface port = service.getPort(ServiceInterface.class);
      assertEquals("(B) Hi John", port.greetMe("John"));
   }

}
