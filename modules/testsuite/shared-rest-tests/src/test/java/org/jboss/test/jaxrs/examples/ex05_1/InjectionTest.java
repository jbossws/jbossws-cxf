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
package org.jboss.test.jaxrs.examples.ex05_1;

import java.io.File;
import java.net.URL;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author <a href="mailto:alessio.soldano@jboss.com">Alessio Soldano</a>
 */
@RunWith(Arquillian.class)
public class InjectionTest extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs-examples-ex05_1.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.jaxrs.examples.ex05_1.domain.Customer.class)
               .addClass(org.jboss.test.jaxrs.examples.ex05_1.services.CustomerResource.class)
               .addClass(org.jboss.test.jaxrs.examples.ex05_1.services.ShoppingApplication.class)
               .addClass(org.jboss.test.jaxrs.examples.ex05_1.services.CarResource.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxrs/examples/ex05_x/WEB-INF/web.xml"));
      return archive;
   }
   
   private static Client client;

   @BeforeClass
   public static void initClient()
   {
      client = ClientBuilder.newClient();
   }

   @AfterClass
   public static void closeClient()
   {
      client.close();
      client = null;
   }

   @Test
   @RunAsClient
   public void testCarResource() throws Exception
   {
      String car = client.target(baseURL + "services/cars/matrix/mercedes/e55;color=black/2006").request().get(String.class);
      Assert.assertEquals("A black 2006 mercedes e55", car);

      car = client.target(baseURL + "services/cars/segment/mercedes/e55;color=black/2006").request().get(String.class);
      Assert.assertEquals("A black 2006 mercedes e55", car);

      car = client.target(baseURL + "services/cars/segments/mercedes/e55/amg/year/2006").request().get(String.class);
      Assert.assertEquals("A 2006 mercedes e55 amg", car);

      car = client.target(baseURL + "services/cars/uriinfo/mercedes/e55;color=black/2006").request().get(String.class);
      Assert.assertEquals("A black 2006 mercedes e55", car);
   }

   @Test
   @RunAsClient
   public void testCustomerResource() throws Exception
   {
      String customer = client.target(baseURL + "services/customers").request().get(String.class);
      Assert.assertTrue(customer.contains("Bill"));
      Assert.assertTrue(customer.contains("Joe"));

      String list = client.target(baseURL + "services/customers")
                          .queryParam("start", "1")
                          .queryParam("size", "3")
                          .request().get(String.class);
      Assert.assertTrue(list.contains("Joe"));
      Assert.assertTrue(list.contains("Monica"));
      Assert.assertTrue(list.contains("Steve"));

      list = client.target(baseURL + "services/customers/uriinfo")
                   .queryParam("start", "2")
                   .queryParam("size", "2")
                   .request().get(String.class);
      Assert.assertTrue(list.contains("Monica"));
      Assert.assertTrue(list.contains("Steve"));
   }
}
