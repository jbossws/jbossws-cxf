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
package org.jboss.test.ws.jaxws.samples.advanced.retail;

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
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Heiko Braun <heiko.braun@jboss.com>
 * @since 08-Nov-2006
 */
@RunWith(Arquillian.class)
public class RetailSampleTestCase extends JBossWSTest {

   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static JavaArchive createDeployments() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-samples-retail.jar");
         archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: org.jboss.logging\n"))
               .addClass(org.jboss.test.ws.jaxws.samples.advanced.retail.Customer.class)
               .addClass(org.jboss.test.ws.jaxws.samples.advanced.retail.Order.OrderState.class)
               .addClass(org.jboss.test.ws.jaxws.samples.advanced.retail.Order.class)
               .addClass(org.jboss.test.ws.jaxws.samples.advanced.retail.OrderAdmin.class)
               .addClass(org.jboss.test.ws.jaxws.samples.advanced.retail.OrderItem.class)
               .addClass(org.jboss.test.ws.jaxws.samples.advanced.retail.OrderLineRequest.class)
               .addClass(org.jboss.test.ws.jaxws.samples.advanced.retail.OrderLineResponse.class)
               .addClass(org.jboss.test.ws.jaxws.samples.advanced.retail.OrderMgmt.class)
               .addClass(org.jboss.test.ws.jaxws.samples.advanced.retail.OrderMgmtBean.class)
               .addClass(org.jboss.test.ws.jaxws.samples.advanced.retail.OrderStatus.class)
               .addClass(org.jboss.test.ws.jaxws.samples.advanced.retail.RetailSampleTestCase.class)
               .addPackage("org.jboss.test.ws.jaxws.samples.advanced.retail.cc")
               .addClass(org.jboss.test.ws.jaxws.samples.advanced.retail.handler.SOAPMessageTrace.class)
               .addClass(org.jboss.test.ws.jaxws.samples.advanced.retail.handler.Timer.class)
               .addAsResource("org/jboss/test/ws/jaxws/samples/advanced/retail/jaxws-handler.xml")
               .addPackage("org.jboss.test.ws.jaxws.samples.advanced.retail.profile")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/advanced/retail/META-INF/permissions.xml"), "permissions.xml")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/advanced/retail/META-INF/wsdl/CCVerificationService.wsdl"), "wsdl/CCVerificationService.wsdl")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/advanced/retail/META-INF/wsdl/OrderMgmtService.wsdl"), "wsdl/OrderMgmtService.wsdl")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/advanced/retail/META-INF/wsdl/ProfileMgmtService.wsdl"), "wsdl/ProfileMgmtService.wsdl");
      return archive;
   }

   protected OrderMgmt getPort() throws Exception
   {
      QName serviceName = new QName("http://retail.advanced.samples.jaxws.ws.test.jboss.org/", "OrderMgmtService");
      URL wsdlURL = new URL(baseURL + "/jaxws-samples-retail/OrderMgmtService/OrderMgmtBean?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      return (OrderMgmt)service.getPort(OrderMgmt.class);
   }


   @Test
   @RunAsClient
   public void testWebService() throws Exception
   {
      Customer customer = new Customer();
      customer.setFirstName("Chuck");
      customer.setLastName("Norris");
      customer.setCreditCardDetails("1000-4567-3456-XXXX");

      Order order = new Order(customer);
      order.setOrderNum(12345);
      order.getItems().add( new OrderItem("Introduction to Web Services", 39.99) );

      OrderStatus result = getPort().prepareOrder(order);
      assertNotNull("Result was null", result);
      assertEquals("Prepared", result.getStatus());
   }
}
