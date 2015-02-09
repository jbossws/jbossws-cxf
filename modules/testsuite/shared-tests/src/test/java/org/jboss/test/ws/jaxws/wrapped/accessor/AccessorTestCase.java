/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.wrapped.accessor;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test different JAXB accesor types.
 *
 * @author <a href="jason.greene@jboss.com">Jason T. Greene</a>
 */
@RunWith(Arquillian.class)
public class AccessorTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   private final String targetNS = "http://accessor.wrapped.jaxws.ws.test.jboss.org/";
   private Accessor proxy;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-wrapped-accessor.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.wrapped.accessor.Accessor.class)
               .addClass(org.jboss.test.ws.jaxws.wrapped.accessor.AccessorImpl.class)
               .addClass(org.jboss.test.ws.jaxws.wrapped.accessor.jaxws.FieldAccessor.class)
               .addClass(org.jboss.test.ws.jaxws.wrapped.accessor.jaxws.FieldAccessorResponse.class)
               .addClass(org.jboss.test.ws.jaxws.wrapped.accessor.jaxws.MethodAccessor.class)
               .addClass(org.jboss.test.ws.jaxws.wrapped.accessor.jaxws.MethodAccessorResponse.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/wrapped/accessor/WEB-INF/web.xml"));
      return archive;
   }

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      QName serviceName = new QName(targetNS, "AccessorService");
      URL wsdlURL = new URL(baseURL + "AccessorService?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      proxy = (Accessor) service.getPort(Accessor.class);
   }

   @Test
   @RunAsClient
   public void testFieldAccessor() throws Exception
   {
      setUp();
      assertEquals("ing123", proxy.fieldAccessor("ing", 123));
   }

   @Test
   @RunAsClient
   public void testMethodAccessor() throws Exception
   {
      setUp();
      assertEquals("moretesting456", proxy.fieldAccessor("moretesting", 456));
   }
}
