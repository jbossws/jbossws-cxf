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
package org.jboss.test.jaxrs.examples.ex04_1;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
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
import java.net.URL;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author <a href="mailto:alessio.soldano@jboss.com">Alessio Soldano</a>
 */
@RunWith(Arquillian.class)
public class PatchTest extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs-examples-ex04_1.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.jaxrs.examples.ex04_1.domain.Customer.class)
               .addClass(org.jboss.test.jaxrs.examples.ex04_1.services.CustomerResource.class)
               .addClass(org.jboss.test.jaxrs.examples.ex04_1.services.ShoppingApplication.class)
               .addClass(org.jboss.test.jaxrs.examples.ex04_1.annotations.PATCH.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxrs/examples/ex04_x/WEB-INF/web.xml"));
      return archive;
   }
   
   private static class HttpPatch extends HttpPost
   {
      public HttpPatch(String s)
      {
         super(s);
      }

      public String getMethod()
      {
         return "PATCH";
      }
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

      DefaultHttpClient client = new DefaultHttpClient();

      HttpPost post = new HttpPost(baseURL + "services/customers");
      StringEntity entity = new StringEntity(newCustomer);
      entity.setContentType("application/xml");
      post.setEntity(entity);
      HttpClientParams.setRedirecting(post.getParams(), false);
      HttpResponse response = client.execute(post);

      Assert.assertEquals(201, response.getStatusLine().getStatusCode());
      Assert.assertTrue(response.getLastHeader("Location").toString().contains("jaxrs-examples-ex04_1/services/customers/1"));

      response.getEntity().getContent().close();

      HttpPatch patch = new HttpPatch(baseURL + "services/customers/1");

      // Update the new customer.  Change Bill's name to William
      String patchCustomer = "<customer>"
            + "<first-name>William</first-name>"
            + "</customer>";
      entity = new StringEntity(patchCustomer);
      entity.setContentType("application/xml");
      patch.setEntity(entity);
      response = client.execute(patch);

      Assert.assertEquals(204, response.getStatusLine().getStatusCode());

      // Show the update
      HttpGet get = new HttpGet(baseURL + "services/customers/1");
      response = client.execute(get);
      Assert.assertEquals(200, response.getStatusLine().getStatusCode());

      Assert.assertTrue(response.getEntity().getContentType().toString().contains("application/xml"));
      BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

      StringBuilder sb = new StringBuilder();
      String line = reader.readLine();
      while (line != null)
      {
         sb.append(line);
         line = reader.readLine();
      }
      Assert.assertTrue(sb.toString().contains("William"));
      response.getEntity().getContent().close();
   }
}
