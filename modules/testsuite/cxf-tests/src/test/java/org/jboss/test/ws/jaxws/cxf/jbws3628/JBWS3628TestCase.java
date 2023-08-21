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
package org.jboss.test.ws.jaxws.cxf.jbws3628;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Testcase for system property expansion support in WSDL documents.
 *
 * @author alessio.soldano@jboss.com
 * @since 21-Jul-2014
 */
@RunWith(Arquillian.class)
public class JBWS3628TestCase extends JBossWSTest
{
   private static final String POLICY_NAME = "WS-Addressing_policy";
   
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-cxf-jbws3628.war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.apache.cxf\n"))
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3628.EndpointOneImpl.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3628.CheckInterceptor.class)
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3628/WEB-INF/wsdl/service.wsdl"), "wsdl/service.wsdl");
      return archive;
   }

   @Test
   @RunAsClient
   public void testWSDL() throws Exception {
      URL wsdlURL = new URL(baseURL + "/ServiceOne" + "?wsdl");
      checkPolicyReference(wsdlURL, POLICY_NAME);
   }
   
   @Test
   @RunAsClient
   public void testInvocation() throws Exception {
      URL wsdlURL = new URL(baseURL + "/ServiceOne" + "?wsdl");
      Service service = Service.create(wsdlURL, new QName("http://org.jboss.ws.jaxws.cxf/jbws3628", "ServiceOne"));
      EndpointOne port = service.getPort(new QName("http://org.jboss.ws.jaxws.cxf/jbws3628", "EndpointOnePort"), EndpointOne.class);
      assertEquals("Foo", port.echo("Foo"));
   }
   
   private void checkPolicyReference(URL wsdlURL, String refId) throws Exception {
      final String wsdl = IOUtils.readAndCloseStream(wsdlURL.openStream());
      assertTrue("WSDL does not contain policy reference to '" + refId + "'", wsdl.contains("<wsp:PolicyReference URI=\"#" + refId + "\"/>"));
   }
   
}
