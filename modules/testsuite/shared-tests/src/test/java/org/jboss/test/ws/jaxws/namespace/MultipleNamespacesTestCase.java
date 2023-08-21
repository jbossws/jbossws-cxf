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
package org.jboss.test.ws.jaxws.namespace;

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
 * Test the JAX-WS metadata builder.
 *
 * @author Heiko.Braun@jboss.org
 * @since 23.01.2007
 */
@RunWith(Arquillian.class)
public class MultipleNamespacesTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-namespace.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.helper.DOMWriter.class)
               .addClass(org.jboss.test.ws.jaxws.namespace.CustomHandler.class)
               .addClass(org.jboss.test.ws.jaxws.namespace.EndpointBean.class)
               .addClass(org.jboss.test.ws.jaxws.namespace.EndpointInterface.class)
               .addAsResource("org/jboss/test/ws/jaxws/namespace/handler-chain.xml")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/namespace/WEB-INF/jboss-web.xml"), "jboss-web.xml")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/namespace/WEB-INF/web.xml"));
      return archive;
   }

   /**
    * If the @WebService.targetNamespace annotation is on a service implementation bean that does NOT reference a service
    * endpoint interface (through the endpointInterface annotation element), the targetNamespace is used for both the
    * wsdl:portType and the wsdl:service (and associated XML elements).
    *
    * If the @WebService.targetNamespace annotation is on a service implementation bean that does reference a service endpoint
    * interface (through the endpointInterface annotation element), the targetNamespace is used for only the wsdl:service (and
    * associated XML elements).
    */
   @Test
   @RunAsClient
   public void testSEIDerivedNamespaces() throws Exception
   {
      // Create the port
      URL wsdlURL = new URL(baseURL + "?wsdl");
      QName qname = new QName("http://example.org/impl", "EndpointBeanService");
      Service service = Service.create(wsdlURL, qname);
      EndpointInterface port = service.getPort(EndpointInterface.class);

      String helloWorld = "Hello world!";
      String response = port.echo(helloWorld);
      assertEquals(helloWorld, response);
   }
}
