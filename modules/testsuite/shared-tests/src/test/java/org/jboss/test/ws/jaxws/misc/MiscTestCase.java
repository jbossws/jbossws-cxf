/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.misc;

import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Misc tests that require a simple ws endpoint deployment
 * 
 * @author alessio.soldano@jboss.com
 * @since 07-Mar-2014
 */
@RunWith(Arquillian.class)
public class MiscTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static JavaArchive createDeployments() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-misc.jar");
         archive
            .addManifest()
            .addClass(org.jboss.test.ws.jaxws.misc.Endpoint.class)
            .addClass(org.jboss.test.ws.jaxws.misc.EndpointImpl.class);
      return archive;
   }

   @Test
   @RunAsClient
   public void testEndpoint() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/jaxws-misc/endpoint?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/misc", "EndpointService");
      Endpoint port = Service.create(wsdlURL, serviceName).getPort(Endpoint.class);
      String retObj = port.echo("Hello");
      assertEquals("Hello", retObj);
   }

   /**
    * [JBWS-3741] WebService doesn't support "//"
    * 
    */
   @Test
   @RunAsClient
   public void testJBWS3741() throws Exception
   {
      assertTrue(IOUtils.readAndCloseStream(new URL(baseURL + "//jaxws-misc/endpoint?wsdl").openStream()).contains("wsdl:definitions"));
      assertTrue(IOUtils.readAndCloseStream(new URL(baseURL + "/jaxws-misc///endpoint?wsdl").openStream()).contains("wsdl:definitions"));
   }

   /**
    * [JBWS-3743] Block HTTP GET requests with no query string
    * 
    */
   @Test
   @RunAsClient
   public void testJBWS3743() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/jaxws-misc/endpoint");
      final HttpURLConnection c = (HttpURLConnection)wsdlURL.openConnection();
      c.connect();
      assertEquals(405, c.getResponseCode());
      String error = IOUtils.readAndCloseStream(c.getErrorStream());
      c.disconnect();
      assertEquals("HTTP GET not supported", error);
   }
}
