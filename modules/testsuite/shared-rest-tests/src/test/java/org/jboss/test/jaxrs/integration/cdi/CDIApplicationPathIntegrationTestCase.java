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
package org.jboss.test.jaxrs.integration.cdi;

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
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
public class CDIApplicationPathIntegrationTestCase
{

   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments()
   {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxrs-integration-cdi-application-path.war");
      archive.addManifest().addClasses(CDIResource.class, CDIBean.class, CDIPathApplication.class)
            .add(EmptyAsset.INSTANCE, "WEB-INF/beans.xml");
      return archive;
   }

   private String performCall(String urlPattern) throws Exception
   {
      return HttpRequest.get(baseURL + urlPattern, 10, TimeUnit.SECONDS);
   }

   @Test
   public void testJaxRsWithApplication() throws Exception
   {
      String result = performCall("cdipath/cdiInject");
      assertEquals("Hello World!", result);
   }

}
