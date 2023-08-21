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
package org.jboss.test.ws.jaxws.jbws1694;

import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Holder;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Heiko.Braun@jboss.com
 */
@RunWith(Arquillian.class)
public class JBWS1694TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static JavaArchive createDeployments() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws1694.jar");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws1694.Basket.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1694.BasketEntries.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1694.Header.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1694.JBWS1694Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1694.JBWS1694EndpointSEI.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1694.JBWS1694TestCase.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1694.Receipt.class);
      return archive;
   }

   @Test
   @RunAsClient
   public void testInheritanceRpc() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/jbws1694/JBWS1694Endpoint?wsdl");
      QName serviceName = new QName("http://jbws1694.jaxws.ws.test.jboss.org/", "JBWS1694EndpointService");
      Service service = Service.create(wsdlURL, serviceName);

      JBWS1694EndpointSEI port = service.getPort(JBWS1694EndpointSEI .class);

      Header inout = new Header();
      inout.setUuid("1234");

      Basket basket = new Basket();
      basket.setCustomerId("4567");

      Receipt receipt = port.submitBasket(new Holder<Header>(inout), basket);
      assertTrue(receipt.getMsg().equals("1234"));
   }
}
