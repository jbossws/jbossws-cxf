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
package org.jboss.test.ws.jaxws.jbws2449;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.RespectBindingFeature;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-2449] Test RespectBindingFeature
 * 
 * @author alessio.soldano@jboss.com
 * @since 15-Jan-2009
 */
@RunWith(Arquillian.class)
public class JBWS2449TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static JavaArchive createDeployments() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws2449.jar");
         archive
            .addManifest()
            .addClass(org.jboss.test.ws.jaxws.jbws2449.Endpoint.class)
            .addClass(org.jboss.test.ws.jaxws.jbws2449.EndpointImpl.class)
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2449/META-INF/wsdl/test.wsdl"), "wsdl/test.wsdl");
      return archive;
   }

   @Test
   @RunAsClient
   public void test() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/jaxws-jbws2449?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/jbws2449", "EndpointService");
      Endpoint port = Service.create(wsdlURL, serviceName).getPort(Endpoint.class);
      String retObj = port.echo("Hello");
      assertEquals("Hello", retObj);
   }

   @Test
   @RunAsClient
   public void testWithRespectBinding() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/jaxws-jbws2449?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/jbws2449", "EndpointService");
      try
      {
         Endpoint ep = Service.create(wsdlURL, serviceName).getPort(Endpoint.class, new RespectBindingFeature(true));
         ep.echo("hi");
         fail("Exception expected, the wsdl has a not understood required extensibility element!");
      }
      catch (Exception e)
      {
         //NOOP
      }
   }

   @Test
   @RunAsClient
   public void testWithRespectBinding2() throws Exception
   {
     URL wsdlURL = new URL(baseURL + "/jaxws-jbws2449?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/jbws2449", "EndpointService");
      Endpoint port = Service.create(wsdlURL, serviceName).getPort(Endpoint.class, new RespectBindingFeature(false));
      String retObj = port.echo("Hello");
      assertEquals("Hello", retObj);
   }

}
