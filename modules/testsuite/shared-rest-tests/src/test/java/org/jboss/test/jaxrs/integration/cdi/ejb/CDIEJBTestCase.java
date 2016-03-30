/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.test.jaxrs.integration.cdi.ejb;

import java.net.URL;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@Ignore
@RunWith(Arquillian.class)
@RunAsClient
public class CDIEJBTestCase
{

   @ArquillianResource
   private URL baseURL;

   private Client client = ClientBuilder.newClient();

   @Deployment(testable = false)
   public static WebArchive createDeployments()
   {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs-integration-cdiejb.war");
      archive
            .addManifest()
            .addClasses(CDIEJBApplication.class, EJBLocal.class, EJBLocalBean.class, EJBResource.class,
                  EJBRootResource.class).addAsWebInfResource(getWebXml(), "web.xml");
      return archive;
   }

   private static StringAsset getWebXml()
   {
      return new StringAsset(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                  + "<web-app xmlns=\"http://java.sun.com/xml/ns/javaee\" "
                  + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                  + "xsi:schemaLocation=\"http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd\" "
                  + "version=\"3.0\">" + "<servlet>" + "    <servlet-name>Mapping</servlet-name>"
                  + "    <servlet-class>org.jboss.wsf.stack.cxf.JAXRSServletExt</servlet-class>" + "    <init-param>"
                  + "         <param-name>javax.ws.rs.Application</param-name>"
                  + "         <param-value>org.jboss.test.jaxrs.integration.cdi.ejb.CDIEJBApplication</param-value>"
                  + "     </init-param>" + "</servlet>" + "<servlet-mapping>"
                  + "    <servlet-name>Mapping</servlet-name>" + "    <url-pattern>/*</url-pattern>"
                  + "</servlet-mapping></web-app>");
   }

   @Test
   public void testRootResource(String urlPattern) throws Exception
   {
      WebTarget target = client.target(baseURL + "/root");
      Invocation.Builder builder = target.request();
      Response response = builder.buildGet().invoke();

      Assert.assertEquals(200, response.getStatus());
      Assert.assertTrue(response.readEntity(String.class).contains("From EJB Root Resource"));
   }

   @Test
   public void testSubResource(String urlPattern) throws Exception
   {
      WebTarget target = client.target(baseURL + "/root/sub");
      Invocation.Builder builder = target.request();
      Response response = builder.buildGet().invoke();

      Assert.assertEquals(200, response.getStatus());
      Assert.assertTrue(response.readEntity(String.class).contains("From EJB Resource"));
   }

   @Test
   public void testLocalEjbSubResource(String urlPattern) throws Exception
   {
      WebTarget target = client.target(baseURL + "/root/local");
      Invocation.Builder builder = target.request();
      Response response = builder.buildGet().invoke();

      Assert.assertEquals(200, response.getStatus());
      Assert.assertTrue(response.readEntity(String.class).contains("From EJBLocalBean"));
   }

   @Test
   public void testUnwrapException(String urlPattern) throws Exception
   {
      WebTarget target = client.target(baseURL + "/root/exception");
      Invocation.Builder builder = target.request();
      Response response = builder.buildGet().invoke();

      Assert.assertEquals(201, response.getStatus());
   }

   @Test
   public void testPriorPostConstructor(String urlPattern) throws Exception
   {
      WebTarget target = client.target(baseURL + "/root/priorpost");
      Invocation.Builder builder = target.request();
      Response response = builder.buildGet().invoke();

      Assert.assertEquals(200, response.getStatus());
      Assert.assertTrue(response.readEntity(String.class).contains("true"));
   }

}
