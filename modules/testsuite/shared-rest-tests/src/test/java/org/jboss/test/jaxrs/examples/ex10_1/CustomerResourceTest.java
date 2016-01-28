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
package org.jboss.test.jaxrs.examples.ex10_1;

import java.io.File;
import java.net.URI;
import java.net.URL;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.jaxrs.examples.ex10_1.domain.Customers;
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
public class CustomerResourceTest extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs-examples-ex10_1.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.jaxrs.examples.ex10_1.domain.Customer.class)
               .addClass(org.jboss.test.jaxrs.examples.ex10_1.domain.Customers.class)
               .addClass(org.jboss.test.jaxrs.examples.ex10_1.services.CustomerResource.class)
               .addClass(org.jboss.test.jaxrs.examples.ex10_1.services.ShoppingApplication.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxrs/examples/ex10_x/WEB-INF/web.xml"));
      return archive;
   }
   
   @Test
   @RunAsClient
   public void testQueryCustomers() throws Exception
   {
      URI uri = new URI(baseURL + "services/customers?start=0&size=5");
      Client client = ClientBuilder.newClient();
      StringBuilder sb = new StringBuilder();
      try {
         while (uri != null)
         {
            WebTarget target = client.target(uri);
            String output = target.request().get(String.class);
            sb.append(output);
   
            Customers customers = target.request().get(Customers.class);
            uri = customers.getNext();
         }
         String s = sb.toString();
         Assert.assertTrue(s.contains("Bill"));
         Assert.assertTrue(s.contains("Joe"));
         Assert.assertTrue(s.contains("Monica"));
         Assert.assertTrue(s.contains("Steve"));
         Assert.assertTrue(s.contains("Rod"));
         Assert.assertFalse(s.contains("Bob"));
      } finally {
         client.close();
      }
   }

   @Test
   @RunAsClient
   public void testQueryCustomers2() throws Exception
   {
      URI uri = new URI(baseURL + "services/customers");
      Client client = ClientBuilder.newClient();
      StringBuilder sb = new StringBuilder();
      try {
         while (uri != null)
         {
            WebTarget target = client.target(uri);
            String output = target.request().get(String.class);
            sb.append(output);
   
            Customers customers = target.request().get(Customers.class);
            uri = customers.getNext();
         }
         String s = sb.toString();
         Assert.assertTrue(s.contains("Bill"));
         Assert.assertTrue(s.contains("Joe"));
         Assert.assertFalse(s.contains("Monica"));
         Assert.assertFalse(s.contains("Steve"));
         Assert.assertFalse(s.contains("Rod"));
         Assert.assertFalse(s.contains("Bob"));
      } finally {
         client.close();
      }
   }
}
