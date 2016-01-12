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
package org.jboss.test.jaxrs.examples.ex11_1;

import java.io.File;
import java.net.URL;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.jaxrs.examples.ex10_2.domain.Customer;
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
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs-examples-ex11_1.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.jaxrs.examples.ex11_1.domain.Customer.class)
               .addClass(org.jboss.test.jaxrs.examples.ex11_1.services.CustomerResource.class)
               .addClass(org.jboss.test.jaxrs.examples.ex11_1.services.ShoppingApplication.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxrs/examples/ex11_x/WEB-INF/web.xml"));
      return archive;
   }
   
   @Test
   @RunAsClient
   public void testCustomerResource() throws Exception
   {
      Client client = ClientBuilder.newClient();
      try {
         WebTarget customerTarget = client.target(baseURL + "services/customers/1");
         Response response = customerTarget.request().get();
         Assert.assertEquals(200, response.getStatus());
         Customer cust = response.readEntity(Customer.class);

         EntityTag etag = response.getEntityTag();
         response.close();

         response = customerTarget.request()
                                  .header("If-None-Match", etag).get();
         Assert.assertEquals(304, response.getStatus());
         response.close();

         // Update and send a bad etag with conditional PUT
         cust.setCity("Bedford");
         response = customerTarget.request()
                 .header("If-Match", "JUNK")
                 .put(Entity.xml(cust));
         Assert.assertEquals(412, response.getStatus());
         response.close();
      } finally {
         client.close();
      }
   }
}
