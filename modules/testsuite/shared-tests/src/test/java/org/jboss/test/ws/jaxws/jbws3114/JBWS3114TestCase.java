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
package org.jboss.test.ws.jaxws.jbws3114;

import java.io.File;
import java.net.URL;

import jakarta.xml.ws.BindingProvider;

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
 * https://jira.jboss.org/browse/JBWS-3114
 * @author ema@redhat.com
 */
@RunWith(Arquillian.class)
public class JBWS3114TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws3114.war");
         archive
            .addManifest()
            .addClass(org.jboss.test.ws.jaxws.jbws3114.Endpoint.class)
            .addClass(org.jboss.test.ws.jaxws.jbws3114.EndpointImpl.class)
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3114/WEB-INF/jboss-web.xml"), "jboss-web.xml")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3114/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testConfigureTimeout() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "?wsdl");
      EndpointService service = new EndpointService(wsdlURL);
      Endpoint port = service.getEndpointPort();
      String response = port.echo("testjbws3114");
      assertEquals("testjbws3114", response);
      ((BindingProvider) port).getRequestContext().put("jakarta.xml.ws.client.connectionTimeout", "6000");
      ((BindingProvider) port).getRequestContext().put("jakarta.xml.ws.client.receiveTimeout", "1000");
      try
      {
         port.echo("testjbws3114");
         fail("Timeout exeception is expected");
      }
      catch (Exception e)
      {
         //expected
      }

   }
}