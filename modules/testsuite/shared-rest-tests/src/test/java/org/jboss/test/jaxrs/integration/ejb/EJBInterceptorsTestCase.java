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
package org.jboss.test.jaxrs.integration.ejb;

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.HttpRequest;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests injections of CDI beans into JAX-RS resources
 *
 * @author Stuart Douglas
 * @author alessio.soldano@jboss.com
 */
@RunWith(Arquillian.class)
@RunAsClient
public class EJBInterceptorsTestCase
{

   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments()
   {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs-integration-ejb.war");
      archive.addManifest().addClasses(EJBResource.class, EjbInterceptor.class, EjbInterface.class)
            .add(EmptyAsset.INSTANCE, "WEB-INF/beans.xml")
            .addAsWebInfResource(getWebXml(), "web.xml");
      return archive;
   }

   private static StringAsset getWebXml()
   {
      return new StringAsset(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                  + "<web-app xmlns=\"http://java.sun.com/xml/ns/javaee\" "
                  + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                  + "xsi:schemaLocation=\"http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd\" "
                  + "version=\"3.0\"><servlet-mapping><servlet-name>javax.ws.rs.core.Application"
                  + "</servlet-name><url-pattern>/myjaxrs/*</url-pattern></servlet-mapping></web-app>");
   }
   
   private String performCall(String urlPattern) throws Exception
   {
      return HttpRequest.get(baseURL + urlPattern, 10, TimeUnit.SECONDS);
   }

   @Test
   public void testJaxRsWithNoApplication() throws Exception
   {
      String result = performCall("myjaxrs/ejbInterceptor");
      assertEquals("Hello World", result);
   }

}
