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
package org.jboss.test.ws.jaxws.jbws3870;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-3870] Imported schemas in weird places in deployments causes issues
 *
 * @author alessio.soldano@jboss.com
 * @since 19-Jul-2015
 */
@RunWith(Arquillian.class)
public class JBWS3870TestCase extends JBossWSTest
{
   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws3870.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws3870.SayHi.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3870.SayHiImpl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3870.sayhi1.SayHi.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3870.sayhi1.SayHiResponse.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3870.sayhi2.SayHiArray.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3870.sayhi2.SayHiArrayResponse.class)
               .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3870/wsdl/thewsdl/sayHi.wsdl"), "wsdl/thewsdl/sayHi.wsdl")
               .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3870/wsdl/sayhi/a.wsdl"), "wsdl/sayhi/a.wsdl")
               .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3870/wsdl/sayhi/sayhi-schema1.xsd"), "wsdl/sayhi/sayhi-schema1.xsd")
               .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3870/wsdl/sayhi/sayhi/a.wsdl"), "wsdl/sayhi/sayhi/a.wsdl")
               .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3870/wsdl/sayhi/sayhi/sayhi-schema1.xsd"), "wsdl/sayhi/sayhi/sayhi-schema1.xsd");
     return archive;
   }

   @Test
   @RunAsClient
   public void testService() throws Exception
   {
      String endpointAddress = "http://" + getServerHost() + ":" + getServerPort() + "/jaxws-jbws3870/SayHiImpl";
      Service service = Service.create(new URL(endpointAddress + "?wsdl"), new QName("http://apache.org/sayHi", "SayHiService"));
      SayHi port = service.getPort(new QName("http://apache.org/sayHi", "SayHiPort"), SayHi.class);
      assertEquals("Hi", port.sayHi("Foo"));
      assertEquals("Hi", port.sayHiArray(new ArrayList<String>()).iterator().next());
   }

}
