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
package org.jboss.test.ws.jaxws.anonymous;

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
 * Test anonymous bare types.
 *
 * @author <a href="jason.greene@jboss.com">Jason T. Greene</a>
 */
@RunWith(Arquillian.class)
public class AnonymousTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-anonymous.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.anonymous.Anonymous.class)
               .addClass(org.jboss.test.ws.jaxws.anonymous.AnonymousImpl.class)
               .addClass(org.jboss.test.ws.jaxws.anonymous.AnonymousRequest.class)
               .addClass(org.jboss.test.ws.jaxws.anonymous.AnonymousResponse.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/anonymous/WEB-INF/web.xml"));
      return archive;
   }


   @Test
   @RunAsClient
   public void testEcho() throws Exception
   {
      QName serviceName = new QName("http://anonymous.jaxws.ws.test.jboss.org/", "AnonymousService");
      URL wsdlURL = new URL(baseURL + "AnonymousService?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      Anonymous proxy = (Anonymous) service.getPort(Anonymous.class);
      
      AnonymousRequest req = new AnonymousRequest();
      req.message = "echo123";
      assertEquals("echo123", proxy.echoAnonymous(req).message);
   }
}
