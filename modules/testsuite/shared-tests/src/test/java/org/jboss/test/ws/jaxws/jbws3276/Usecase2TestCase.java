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

package org.jboss.test.ws.jaxws.jbws3276;

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
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-3276] Tests anonymous POJO in web archive that is missing web.xml.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
@RunWith(Arquillian.class)
public class Usecase2TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws3276-usecase2.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws3276.AnonymousPOJO.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3276.POJOIface.class);
      return archive;
   }

   @Test
   @RunAsClient
   public void testAnonymousEndpoint() throws Exception
   {
      final QName serviceName = new QName("org.jboss.test.ws.jaxws.jbws3276", "AnonymousPOJOService");
      final URL wsdlURL = new URL(baseURL +  "/AnonymousPOJOService?wsdl");
      final Service service = Service.create(wsdlURL, serviceName);
      final POJOIface port = service.getPort(POJOIface.class);
      final String result = port.echo("hello");
      assertEquals("hello from anonymous POJO", result);
   }

}
