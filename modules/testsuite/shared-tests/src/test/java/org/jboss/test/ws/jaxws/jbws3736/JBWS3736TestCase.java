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
package org.jboss.test.ws.jaxws.jbws3736;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-3736] soap:address rewrite does not consider wsdlLocation in SEI @WebService
 * 
 * @author alessio.soldano@jboss.com
 * @since 07-Mar-2014
 */
@RunWith(Arquillian.class)
public class JBWS3736TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static JavaArchive createDeployments() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws3736.jar");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws3736.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3736.EndpointImpl.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3736/META-INF/wsdl/test.wsdl"), "wsdl/test.wsdl");
      return archive;
   }

   @Test
   @RunAsClient
   public void testEndpoint() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/jaxws-jbws3736?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/jbws3736", "EndpointService");
      Endpoint port = Service.create(wsdlURL, serviceName).getPort(Endpoint.class);
      ((BindingProvider)port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, baseURL.toString() + "/jaxws-jbws3736");
      String retObj = port.echo("Hello");
      assertEquals("Hello", retObj);
   }

   @Test
   @RunAsClient
   public void testAddressRewrite() throws Exception
   {
      String wsdl = IOUtils.readAndCloseStream(new URL(baseURL + "/jaxws-jbws3736?wsdl").openStream());

      String serverHost = getServerHost().replace("127.0.0.1", "localhost"); //because of TCK workaround in org.jboss.ws.common.management.AbstractServerConfig
      //we expect the published wsdl to have the https protocol in the soap:address because the original wsdl provided
      //in the deployment has that. This shows that the reference to the wsdl in endpoint interface has been processed
      //when rewriting the soap:address. If we got http protocol here, the fix won't be in place.
      assertTrue(wsdl.contains("https://" + serverHost + ":" + (getServerPort() + 363) + "/jaxws-jbws3736"));
   }
}
