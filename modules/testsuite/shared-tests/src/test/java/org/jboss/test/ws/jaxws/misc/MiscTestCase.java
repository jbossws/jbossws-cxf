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
package org.jboss.test.ws.jaxws.misc;

import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Misc tests that require a simple ws endpoint deployment
 * 
 * @author alessio.soldano@jboss.com
 * @since 07-Mar-2014
 */
@RunWith(Arquillian.class)
public class MiscTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static JavaArchive createDeployments() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-misc.jar");
         archive
            .addManifest()
            .addClass(org.jboss.test.ws.jaxws.misc.Endpoint.class)
            .addClass(org.jboss.test.ws.jaxws.misc.EndpointImpl.class);
      return archive;
   }

   @Test
   @RunAsClient
   public void testEndpoint() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/jaxws-misc/endpoint?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/misc", "EndpointService");
      Endpoint port = Service.create(wsdlURL, serviceName).getPort(Endpoint.class);
      String retObj = port.echo("Hello");
      assertEquals("Hello", retObj);
   }

   /**
    * [JBWS-3741] WebService doesn't support "//"
    * 
    */
   @Test
   @RunAsClient
   public void testJBWS3741() throws Exception
   {
      assertTrue(IOUtils.readAndCloseStream(new URL(baseURL + "//jaxws-misc/endpoint?wsdl").openStream()).contains("wsdl:definitions"));
      assertTrue(IOUtils.readAndCloseStream(new URL(baseURL + "/jaxws-misc///endpoint?wsdl").openStream()).contains("wsdl:definitions"));
   }

   /**
    * [JBWS-3743] Block HTTP GET requests with no query string
    * 
    */
   @Test
   @RunAsClient
   public void testJBWS3743() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/jaxws-misc/endpoint");
      final HttpURLConnection c = (HttpURLConnection)wsdlURL.openConnection();
      c.connect();
      assertEquals(405, c.getResponseCode());
      String error = IOUtils.readAndCloseStream(c.getErrorStream());
      c.disconnect();
      assertEquals("HTTP GET not supported", error);
   }
}
