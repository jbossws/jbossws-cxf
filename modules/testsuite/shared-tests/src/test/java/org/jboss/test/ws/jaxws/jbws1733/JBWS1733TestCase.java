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
package org.jboss.test.ws.jaxws.jbws1733;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
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
 * [JBWS-1733] JBoss WebService client not sending JSESSIONID cookie after 2 calls
 *
 * @author ropalka@redhat.com
 * @since 09-Aug-2007
 */
@RunWith(Arquillian.class)
public class JBWS1733TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws1733.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws1733.JBWS1733.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1733.JBWS1733Impl.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1733/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testIssue() throws Exception
   {
      QName serviceName = new QName("http://jbws1733.jaxws.ws.test.jboss.org/", "JBWS1733Service");
      URL wsdlURL = new URL(baseURL + "/JBWS1733Service?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      JBWS1733 proxy = (JBWS1733)service.getPort(JBWS1733.class);
      
      ((BindingProvider)proxy).getRequestContext().put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
      for (int i = 1; i <= 10; i++)
      {
         assertEquals(i, proxy.getCounter());
      }
   }

}
