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
package org.jboss.test.ws.jaxws.jbws1446;

import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-1446] Invalid WSDL when a parameter of type Object is part of a webmethod
 *
 * http://jira.jboss.org/jira/browse/JBWS-1446
 *
 * @author Thomas.Diesler@jboss.com
 * @since 20-Jun-2007
 */
@RunWith(Arquillian.class)
public class JBWS1446TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static JavaArchive createDeployments() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws1446.jar");
         archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: org.jboss.logging\n"))
               .addClass(org.jboss.test.ws.jaxws.jbws1446.EJB3Bean.class);
      return archive;
   }

   @Test
   @RunAsClient
   public void testObjectAccess() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/jaxws-jbws1446/EJB3Bean?wsdl");
      QName serviceName = new QName("http://jbws1446.jaxws.ws.test.jboss.org/", "EJB3BeanService");
      Service service = Service.create(wsdlURL, serviceName);
      EndpointInterface port = service.getPort(EndpointInterface.class);
      
      Object hello = port.helloObject("hello");
      assertEquals("hello", hello);
   }
}
