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
package org.jboss.test.jaxrs.examples.ex04_2;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs-examples-ex04_2.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.jaxrs.examples.ex04_2.domain.Customer.class)
               .addClass(org.jboss.test.jaxrs.examples.ex04_2.services.CustomerResource.class)
               .addClass(org.jboss.test.jaxrs.examples.ex04_2.services.ShoppingApplication.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxrs/examples/ex04_x/WEB-INF/web.xml"));
      return archive;
   }
   
   @Test
   @RunAsClient
   public void testCustomerResource() throws Exception
   {
      // Create a new customer
      String newCustomer = "<customer>"
              + "<first-name>Bill</first-name>"
              + "<last-name>Burke</last-name>"
              + "<street>256 Clarendon Street</street>"
              + "<city>Boston</city>"
              + "<state>MA</state>"
              + "<zip>02115</zip>"
              + "<country>USA</country>"
              + "</customer>";

      URL postUrl = new URL(baseURL + "services/customers");
      HttpURLConnection connection = (HttpURLConnection) postUrl.openConnection();
      connection.setDoOutput(true);
      connection.setInstanceFollowRedirects(false);
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", "application/xml");
      OutputStream os = connection.getOutputStream();
      os.write(newCustomer.getBytes());
      os.flush();
      Assert.assertEquals(HttpURLConnection.HTTP_CREATED, connection.getResponseCode());
      Assert.assertTrue(connection.getHeaderField("Location").toString().contains("jaxrs-examples-ex04_2/services/customers/1"));
      connection.disconnect();


      // Get the new customer
      URL getUrl = new URL(baseURL + "services/customers/1");
      connection = (HttpURLConnection) getUrl.openConnection();
      connection.setRequestMethod("GET");
      Assert.assertTrue(connection.getContentType().contains("application/xml"));

      BufferedReader reader = new BufferedReader(new
              InputStreamReader(connection.getInputStream()));

      String line = reader.readLine();
      StringBuilder sb = new StringBuilder();
      while (line != null)
      {
         sb.append(line);
         line = reader.readLine();
      }
      Assert.assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
      Assert.assertTrue(sb.toString().contains("Bill"));
      connection.disconnect();


      connection.disconnect();

      // use first last
      getUrl = new URL(baseURL + "services/customers/Bill-Burke");
      connection = (HttpURLConnection) getUrl.openConnection();
      connection.setRequestMethod("GET");

      Assert.assertTrue(connection.getContentType().contains("application/xml"));
      reader = new BufferedReader(new
              InputStreamReader(connection.getInputStream()));

      line = reader.readLine();
      sb = new StringBuilder();
      while (line != null)
      {
         sb.append(line);
         line = reader.readLine();
      }
      Assert.assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
      Assert.assertTrue(sb.toString().contains("Bill"));
      connection.disconnect();
   }
}
