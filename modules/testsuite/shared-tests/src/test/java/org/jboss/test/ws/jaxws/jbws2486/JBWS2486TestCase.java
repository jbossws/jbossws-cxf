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
package org.jboss.test.ws.jaxws.jbws2486;

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
 * [JBWS-2486] POJO service should be shared
 *
 * @author richard.opalka@jboss.com
 */
@RunWith(Arquillian.class)
public class JBWS2486TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws2486.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws2486.JBWS2486.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2486.JBWS2486Impl.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2486/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testIssue() throws Exception
   {
      QName serviceName = new QName("http://jbws2486.jaxws.ws.test.jboss.org/", "JBWS2486Service");
      URL wsdlURL = new URL(baseURL + "/JBWS2486Service?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      JBWS2486 proxy = (JBWS2486)service.getPort(JBWS2486.class);
      
      String serviceInstanceId = proxy.getServiceInstanceId();
      for (int i = 1; i <= 10; i++)
      {
         assertEquals(proxy.getServiceInstanceId(), serviceInstanceId);
      }
   }

}
