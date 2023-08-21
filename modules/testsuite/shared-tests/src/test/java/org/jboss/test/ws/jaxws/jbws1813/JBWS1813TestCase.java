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
package org.jboss.test.ws.jaxws.jbws1813;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * context-root in jboss.xml is ignored
 *
 * http://jira.jboss.org/jira/browse/JBWS-1813
 *
 * @author Thomas.Diesler@jboss.com
 * @since 09-Oct-2007
 */
@RunWith(Arquillian.class)
public class JBWS1813TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static JavaArchive createDeployment3() {
      JavaArchive archive1 = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws1813.jar");
         archive1
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws1813.EndpointImpl.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1813/META-INF/jboss-webservices.xml"), "jboss-webservices.xml");
      JBossWSTestHelper.writeToFile(archive1);

      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws1813.ear");
         archive
               .addManifest()
               .addAsResource(new File(JBossWSTestHelper.getTestArchiveDir(), "jaxws-jbws1813.jar"))
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1813/META-INF/application.xml"), "application.xml");
      return archive;
   }

  @Test
  @RunAsClient
   public void testPositive() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/test-context?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/jbws1813", "EndpointService");
      Endpoint port = Service.create(wsdlURL, serviceName).getPort(Endpoint.class);

      String retObj = port.echo("Hello");
      assertEquals("Hello", retObj);
   }
}
