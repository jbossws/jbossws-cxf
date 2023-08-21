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
package org.jboss.test.ws.jaxws.jbws1799;

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
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-1799] Two ejb3s exposed as 2 different web services in the same ear file.
 * Can't have same methods with different parameters in two separate EJBs.
 *
 * @author richard.opalka@jboss.com
 *
 * @since Oct 8, 2007
 */
@RunWith(Arquillian.class)
public class JBWS1799TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static JavaArchive createDeployments() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws1799.jar");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws1799.IUserAccountService.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1799.IUserAccountServiceExt.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1799.UserAccountService.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1799.UserAccountServiceExt.class);
      return archive;
   }

   @Test
   @RunAsClient
   public void testFirstService() throws Exception
   {
      QName serviceName = new QName("namespace1", "UserAccountService1.0");
      URL wsdlURL = new URL(baseURL + "/svc-useracctv1.0/UserAccountService1.0?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      IUserAccountService proxy = (IUserAccountService)service.getPort(IUserAccountService.class);

      assertTrue(proxy.authenticate("authorized"));
      assertFalse(proxy.authenticate("unauthorized"));
   }

   @Test
   @RunAsClient
   public void testSecondService() throws Exception
   {
      QName serviceName = new QName("namespaceExt", "UserAccountServiceExt1.0");
      URL wsdlURL = new URL(baseURL + "/svc-useracctv1.0/UserAccountServiceExt1.0?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      IUserAccountServiceExt proxy = (IUserAccountServiceExt)service.getPort(IUserAccountServiceExt.class);

      assertTrue(proxy.authenticate("authorized", "password"));
      assertFalse(proxy.authenticate("unauthorized", "password"));
   }
}
