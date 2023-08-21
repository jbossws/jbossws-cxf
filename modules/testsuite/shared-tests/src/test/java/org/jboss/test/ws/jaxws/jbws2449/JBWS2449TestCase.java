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
package org.jboss.test.ws.jaxws.jbws2449;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.RespectBindingFeature;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-2449] Test RespectBindingFeature
 * 
 * @author alessio.soldano@jboss.com
 * @since 15-Jan-2009
 */
@RunWith(Arquillian.class)
public class JBWS2449TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static JavaArchive createDeployments() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws2449.jar");
         archive
            .addManifest()
            .addClass(org.jboss.test.ws.jaxws.jbws2449.Endpoint.class)
            .addClass(org.jboss.test.ws.jaxws.jbws2449.EndpointImpl.class)
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2449/META-INF/wsdl/test.wsdl"), "wsdl/test.wsdl");
      return archive;
   }

   @Test
   @RunAsClient
   public void test() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/jaxws-jbws2449?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/jbws2449", "EndpointService");
      Endpoint port = Service.create(wsdlURL, serviceName).getPort(Endpoint.class);
      String retObj = port.echo("Hello");
      assertEquals("Hello", retObj);
   }

   @Test
   @RunAsClient
   public void testWithRespectBinding() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/jaxws-jbws2449?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/jbws2449", "EndpointService");
      try
      {
         Endpoint ep = Service.create(wsdlURL, serviceName).getPort(Endpoint.class, new RespectBindingFeature(true));
         ep.echo("hi");
         fail("Exception expected, the wsdl has a not understood required extensibility element!");
      }
      catch (Exception e)
      {
         //NOOP
      }
   }

   @Test
   @RunAsClient
   public void testWithRespectBinding2() throws Exception
   {
     URL wsdlURL = new URL(baseURL + "/jaxws-jbws2449?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/jbws2449", "EndpointService");
      Endpoint port = Service.create(wsdlURL, serviceName).getPort(Endpoint.class, new RespectBindingFeature(false));
      String retObj = port.echo("Hello");
      assertEquals("Hello", retObj);
   }

}
