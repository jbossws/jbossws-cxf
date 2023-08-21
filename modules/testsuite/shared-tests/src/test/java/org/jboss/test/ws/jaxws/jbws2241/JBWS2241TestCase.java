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
package org.jboss.test.ws.jaxws.jbws2241;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
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
 * [JBWS-2241] Testing url-pattern parameter in jboss.xml
 *
 * @author alessio.soldano@jboss.com
 * @since 29-Sep-2008
 */
@RunWith(Arquillian.class)
public class JBWS2241TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static JavaArchive createDeployments() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws2241.jar");
         archive
            .addManifest()
            .addClass(org.jboss.test.ws.jaxws.jbws2241.EJB3Bean.class)
            .addClass(org.jboss.test.ws.jaxws.jbws2241.EJB3RemoteInterface.class)
            .addClass(org.jboss.test.ws.jaxws.jbws2241.EndpointInterface.class)
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2241/META-INF/jboss-ejb3.xml"), "jboss-ejb3.xml")
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2241/META-INF/jboss-webservices.xml"), "jboss-webservices.xml")
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2241/META-INF/jboss.xml"), "jboss.xml");
      return archive;
   }

   private EndpointInterface getPort(String user, String pwd) throws MalformedURLException {
      URL wsdlURL = new URL(baseURL + "/contextRoot/urlPattern/test?wsdl");
      QName serviceName = new QName("http://jbws2241.jaxws.ws.test.jboss.org/", "EJB3BeanService");
      Service service = Service.create(wsdlURL, serviceName);
      EndpointInterface port = service.getPort(EndpointInterface.class);
      if (user != null) {
         ((BindingProvider)port).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, user);
      }
      if (pwd != null) {
         ((BindingProvider)port).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, pwd);
      }
      return port;
   }

   @Test
   @RunAsClient
   public void testInvocation() throws Exception
   {
      EndpointInterface port = getPort("kermit", "thefrog");
      String hello = port.hello("hello");
      assertEquals("hello", hello);
      
      port = getPort("kermit", "notthefrog");
      try {
         port.hello("hi");
         fail("Failure expected with wrong credentials");
      } catch (Exception e) {
         
      }
      
      port = getPort(null, null);
      try {
         port.hello("hi");
         fail("Failure expected without credentials");
      } catch (Exception e) {
         
      }
   }
}
