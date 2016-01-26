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
package org.jboss.test.jaxrs.examples.asynch;

import java.net.URL;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @author <a href="mailto:alessio.soldano@jboss.com">Alessio Soldano</a>
 */
@RunWith(Arquillian.class)
public class AsyncTest extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs-examples-asynch.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.jaxrs.examples.asynch.MyResource.class)
               .addClass(org.jboss.test.jaxrs.examples.asynch.MyApplication.class)
               .addAsWebInfResource(getWebXml(), "web.xml");
      return archive;
   }
   
   private static StringAsset getWebXml() {
      return new StringAsset(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                  + "<web-app xmlns=\"http://java.sun.com/xml/ns/javaee\" "
                  + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                  + "xsi:schemaLocation=\"http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd\" "
                  + "version=\"3.0\"></web-app>");
   }
   
   @Test
   @RunAsClient
   public void testSuccess() throws Exception
   {
      Client client = ClientBuilder.newClient();
      try {
         Response response = client.target(baseURL + "asynch").request().get();
         Assert.assertEquals(200, response.getStatus());
         Assert.assertEquals("hello", response.readEntity(String.class));
         response.close();
      } finally {
         client.close();
      }
   }

   @Test
   @RunAsClient
   public void testTimeout() throws Exception
   {
      Client client = ClientBuilder.newClient();
      try {
         Response response = client.target(baseURL + "asynch/timeout").request().get();
         Assert.assertEquals(503, response.getStatus());
         response.close();
      } finally {
         client.close();
      }
   }
}
