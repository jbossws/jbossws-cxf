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
package org.jboss.test.ws.jaxws.cxf.jbws3805;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-3805] Allow overriding soap:address rewrite options in jboss-webservices.xml
 *
 */
@RunWith(Arquillian.class)
public class JBWS3805TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-cxf-jbws3805.war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n" + "Dependencies: org.jboss.ws.common\n"))
         .addClass(org.jboss.test.ws.jaxws.cxf.jbws3805.EndpointOne.class).addClass(org.jboss.test.ws.jaxws.cxf.jbws3805.EndpointOneImpl.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3805/WEB-INF/jboss-webservices.xml"), "jboss-webservices.xml")
         .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3805/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testWsdlSoapAddress() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/HelloService?wsdl");
      HttpURLConnection connection = (HttpURLConnection)wsdlURL.openConnection();
      try
      {
         connection.connect();
         assertEquals(200, connection.getResponseCode());
         connection.getInputStream();

         BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
         String line;
         while ((line = in.readLine()) != null)
         {
            if (line.contains("address location"))
            {
               assertTrue("Unexpected uri scheme", line.contains("https://foo:" + (baseURL.getPort() + 8443 - 8080) + "/jaxws-cxf-JBWS3805/HelloService"));
               return;
            }
         }
         fail("Could not check soap:address!");
      }
      finally
      {
         connection.disconnect();
      }

   }

}
