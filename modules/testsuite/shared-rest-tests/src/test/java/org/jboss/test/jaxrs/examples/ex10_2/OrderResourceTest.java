/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.test.jaxrs.examples.ex10_2;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.jaxrs.examples.ex10_2.domain.Customer;
import org.jboss.test.jaxrs.examples.ex10_2.domain.LineItem;
import org.jboss.test.jaxrs.examples.ex10_2.domain.Order;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author <a href="mailto:alessio.soldano@jboss.com">Alessio Soldano</a>
 */
@RunWith(Arquillian.class)
public class OrderResourceTest extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs-examples-ex10_2.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.jaxrs.examples.ex10_2.domain.Customer.class)
               .addClass(org.jboss.test.jaxrs.examples.ex10_2.domain.Customers.class)
               .addClass(org.jboss.test.jaxrs.examples.ex10_2.domain.Order.class)
               .addClass(org.jboss.test.jaxrs.examples.ex10_2.domain.Orders.class)
               .addClass(org.jboss.test.jaxrs.examples.ex10_2.domain.LineItem.class)
               .addClass(org.jboss.test.jaxrs.examples.ex10_2.services.CustomerResource.class)
               .addClass(org.jboss.test.jaxrs.examples.ex10_2.services.OrderResource.class)
               .addClass(org.jboss.test.jaxrs.examples.ex10_2.services.StoreResource.class)
               .addClass(org.jboss.test.jaxrs.examples.ex10_2.services.ShoppingApplication.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxrs/examples/ex10_x/WEB-INF/web.xml"));
      return archive;
   }
   
   @Test
   @RunAsClient
   public void testCreateCancelPurge() throws Exception
   {
      Client client = ClientBuilder.newClient();
      try {
         Response response = client.target(baseURL + "services/shop").request().head();

         Link customers = response.getLink("customers");
         Link orders = response.getLink("orders");
         response.close();

         Assert.assertTrue(customers.getUri().toString().contains("jaxrs-examples-ex10_2/services/customers"));

         Customer customer = new Customer();
         customer.setFirstName("Bill");
         customer.setLastName("Burke");
         customer.setStreet("10 Somewhere Street");
         customer.setCity("Westford");
         customer.setState("MA");
         customer.setZip("01711");
         customer.setCountry("USA");

         response = client.target(customers).request().post(Entity.xml(customer));
         Assert.assertEquals(201, response.getStatus());
         response.close();


         Order order = new Order();
         order.setTotal("$199.99");
         order.setCustomer(customer);
         order.setDate(new Date().toString());
         LineItem item = new LineItem();
         item.setCost("$199.99");
         item.setProduct("iPhone");
         order.setLineItems(new ArrayList<LineItem>());
         order.getLineItems().add(item);

         Assert.assertTrue(orders.getUri().toString().contains("jaxrs-examples-ex10_2/services/orders"));
         response = client.target(orders).request().post(Entity.xml(order));
         Assert.assertEquals(201, response.getStatus());
         URI createdOrderUrl = response.getLocation();
         response.close();

         response = client.target(orders).request().get();
         String orderList = response.readEntity(String.class);
         Assert.assertTrue(orderList.contains("<product>iPhone</product>"));
         Assert.assertTrue(orderList.contains("<cancelled>false</cancelled>"));
         Link purge = response.getLink("purge");
         response.close();

         response = client.target(createdOrderUrl).request().head();
         Link cancel = response.getLink("cancel");
         response.close();
         if (cancel != null)
         {
            Assert.assertTrue(cancel.getUri().toString().contains("jaxrs-examples-ex10_2/services/orders/1/cancel"));
            response = client.target(cancel).request().post(null);
            Assert.assertEquals(204, response.getStatus());
            response.close();
         }

         orderList = client.target(orders).request().get(String.class);
         Assert.assertTrue(orderList.contains("<product>iPhone</product>"));
         Assert.assertTrue(orderList.contains("<cancelled>true</cancelled>"));

         response = client.target(purge).request().post(null);
         Assert.assertEquals(204, response.getStatus());
         response.close();

         orderList = client.target(orders).request().get(String.class);
         Assert.assertFalse(orderList.contains("<product>iPhone</product>"));
      } finally {
         client.close();
      }
   }
}
