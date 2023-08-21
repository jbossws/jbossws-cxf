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
package org.jboss.test.ws.jaxws.jbws2957;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.ws.jaxws.jbws2957.common.HelloIface;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-2957] Tests EJB3 service in web inf lib directory.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
@RunWith(Arquillian.class)
public class JBWS2957TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static EnterpriseArchive createDeployment() {
      JavaArchive archive1 = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws2957-ejbinwarwebinflib_ejb.jar");
         archive1
            .addManifest()
            .addClass(org.jboss.test.ws.jaxws.jbws2957.common.HelloIface.class)
            .addClass(org.jboss.test.ws.jaxws.jbws2957.common.HelloImpl.class);

      WebArchive archive2 = ShrinkWrap.create(WebArchive.class, "jaxws-jbws2957-ejbinwarwebinflib_web.war");
         archive2
            .addManifest()
            .addAsLibraries(archive1)
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2957/WEB-INF/ejb-jar.xml"), "ejb-jar.xml")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2957/WEB-INF/wsdl/HelloService.wsdl"), "wsdl/HelloService.wsdl");

      EnterpriseArchive archive = ShrinkWrap.create(EnterpriseArchive.class, "jaxws-jbws2957-ejbinwarwebinflib.ear");
            archive.addManifest().addAsModule(archive2);
      return archive;
   }

   @Test
   @RunAsClient
   public void testEJB() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/Service/HelloImpl?wsdl");
      QName serviceName = new QName("http://www.jboss.org/test/ws/jaxws/jbws2957", "Service");
      Service.create(wsdlURL, serviceName);
      Service service = Service.create(wsdlURL, serviceName);
      HelloIface port = (HelloIface)service.getPort(HelloIface.class);
      assertEquals("Hello", port.sayHello());
   }
}
