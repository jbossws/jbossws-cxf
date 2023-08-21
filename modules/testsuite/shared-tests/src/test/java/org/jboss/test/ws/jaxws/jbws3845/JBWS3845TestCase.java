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
package org.jboss.test.ws.jaxws.jbws3845;

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
 * [JBWS-3845] Injection not working in JAX-WS handlers from predefined configurations
 *
 * @author alessio.soldano@jboss.com
 * @since 03-Mar-2015
 */
@RunWith(Arquillian.class)
public class JBWS3845TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws3845.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws3845.MyBean.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3845.ServiceImpl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3845.ServiceInterface.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3845.CDIHandler.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3845/WEB-INF/beans.xml"), "beans.xml")
               .addAsResource("org/jboss/test/ws/jaxws/jbws3845/jaxws-endpoint-config.xml", "jaxws-endpoint-config.xml")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3845/WEB-INF/web.xml"));
     return archive;
   }

   @Test
   @RunAsClient
   public void testService() throws Exception
   {
      Service service = Service.create(new URL(baseURL + "/service?wsdl"), new QName("http://org.jboss.ws/jaxws/jbws3845/", "MyService"));
      ServiceInterface port = service.getPort(ServiceInterface.class);
      assertEquals("Greetings Mr. John", port.greetMe("John"));
   }

}
