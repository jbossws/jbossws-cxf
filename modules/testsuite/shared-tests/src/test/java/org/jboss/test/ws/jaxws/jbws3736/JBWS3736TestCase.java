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
package org.jboss.test.ws.jaxws.jbws3736;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-3736] soap:address rewrite does not consider wsdlLocation in SEI @WebService
 * 
 * @author alessio.soldano@jboss.com
 * @since 07-Mar-2014
 */
@RunWith(Arquillian.class)
public class JBWS3736TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static JavaArchive createDeployments() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws3736.jar");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws3736.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3736.EndpointImpl.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3736/META-INF/wsdl/test.wsdl"), "wsdl/test.wsdl");
      return archive;
   }

   @Test
   @RunAsClient
   public void testEndpoint() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/jaxws-jbws3736?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/jbws3736", "EndpointService");
      Endpoint port = Service.create(wsdlURL, serviceName).getPort(Endpoint.class);
      ((BindingProvider)port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, baseURL.toString() + "/jaxws-jbws3736");
      String retObj = port.echo("Hello");
      assertEquals("Hello", retObj);
   }

   @Test
   @RunAsClient
   public void testAddressRewrite() throws Exception
   {
      String wsdl = IOUtils.readAndCloseStream(new URL(baseURL + "/jaxws-jbws3736?wsdl").openStream());

      String serverHost = getServerHost().replace("127.0.0.1", "localhost"); //because of TCK workaround in org.jboss.ws.common.management.AbstractServerConfig
      //we expect the published wsdl to have the https protocol in the soap:address because the original wsdl provided
      //in the deployment has that. This shows that the reference to the wsdl in endpoint interface has been processed
      //when rewriting the soap:address. If we got http protocol here, the fix won't be in place.
      assertTrue(wsdl.contains("https://" + serverHost + ":" + (getServerPort() + 363) + "/jaxws-jbws3736"));
   }
}
