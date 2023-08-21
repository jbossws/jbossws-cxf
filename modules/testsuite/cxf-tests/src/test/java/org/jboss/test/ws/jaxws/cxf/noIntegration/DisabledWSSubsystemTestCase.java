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
package org.jboss.test.ws.jaxws.cxf.noIntegration;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
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
 * Verifies a plain Apache CXF ws endpoint war can be deployed on
 * AS similarly as on a Tomcat instance (the Apache CXF libs from
 * the AS modules are used instead of embedding them in the war).
 * This is is NOT the suggested approach as any Java EE support is
 * actually disabled / skipped (including any JBossWS-CXF
 * integration additions, JSR-109, etc.)
 * 
 * Testcase provided here for the sake of verifying usage of AS as
 * a plain servlet container only, which is sometimes an easy
 * migration path for Apache CXF WS endpoints previously deployed
 * on Tomcat. 
 * 
 * @author alessio.soldano@jboss.com
 * @since 15-Apr-2013
 */
@RunWith(Arquillian.class)
public class DisabledWSSubsystemTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-cxf-disabledWSSubsystem.war");
      archive.addManifest()
         .addClass(org.jboss.test.ws.jaxws.cxf.noIntegration.CXFEndpointServlet.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.noIntegration.EchoImpl.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/noIntegration/moduleImports/WEB-INF/jboss-deployment-structure.xml"), "jboss-deployment-structure.xml")
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/noIntegration/moduleImports/WEB-INF/permissions.xml"), "permissions.xml")
         .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/noIntegration/moduleImports/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testEndpointInvocation() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/services/Echo1?wsdl");
      Service service = Service.create(wsdlURL, new QName("http://org.jboss.ws.jaxws.cxf/noIntegration", "EchoService"));
      Echo echo = service.getPort(new QName("http://org.jboss.ws.jaxws.cxf/noIntegration", "EchoEndpointPort"), Echo.class);
      assertEquals("Foo", echo.echo("Foo"));
   }
   
   @Test
   @RunAsClient
   public void testServicesPage() throws Exception
   {
      URL url = new URL(baseURL + "/services");
      InputStream is = url.openStream();
      assertNotNull(is);
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      try {
         StringBuilder sb = new StringBuilder();
         String line;
         while ((line = reader.readLine()) != null) {
            sb.append(line);
         }
         assertTrue(sb.toString().contains("Available SOAP services:"));
      } finally {
         reader.close();
      }
   }
}
