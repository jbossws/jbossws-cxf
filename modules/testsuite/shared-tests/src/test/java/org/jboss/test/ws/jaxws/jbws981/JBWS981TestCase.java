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
package org.jboss.test.ws.jaxws.jbws981;

import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.wsf.test.IgnoreEnv;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-981] Virtual host configuration for EJB endpoints
 *
 * @author darran.lofthouse@jboss.com
 * @since Nov 2, 2006
 */
@RunWith(Arquillian.class)
public class JBWS981TestCase extends JBossWSTest
{
   //Ignore this test for ipv6; it requires host setting in /etc/hosts [::1 localhost]
   @Rule
   public IgnoreEnv rule = IgnoreEnv.IPV6;
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static JavaArchive createDeployments() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws981.jar");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws981.EJB3Bean.class)
               .addClass(org.jboss.test.ws.jaxws.jbws981.EJB3RemoteInterface.class)
               .addClass(org.jboss.test.ws.jaxws.jbws981.EndpointInterface.class);
      return archive;
   }

   @Test
   @RunAsClient
   public void testCall() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/jaxws-jbws981/EndpointService/EJB3Bean?wsdl");
      QName serviceName = new QName("http://www.jboss.org/test/ws/jaxws/jbws981", "EndpointService");
      Service.create(wsdlURL, serviceName);
      Service service = Service.create(wsdlURL, serviceName);
      EndpointInterface port = (EndpointInterface)service.getPort(EndpointInterface.class);
      
      String message = "Web service mapped to virtual host.";
      assertEquals("Web service mapped to virtual host.", port.hello(message));
   }
}
