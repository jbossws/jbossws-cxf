/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.webservicerefsec;

import java.io.File;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test multiple webserviceref fro the same endpoint with different security credentials
 *
 * @author alessio.soldano@jboss.com
 * @since 12-May-2010
 */
@RunWith(Arquillian.class)
public class WebServiceRefSecTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(name="jaxws-samples-webservicerefsec", order=1, testable = false)
   public static JavaArchive createDeployment() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-samples-webservicerefsec.jar");
      archive
         .addManifest()
         .addClass(org.jboss.test.ws.jaxws.samples.webservicerefsec.EndpointImpl.class);
      return archive;
   }

   @Deployment(name="jaxws-samples-webservicerefsec-servlet-client", order=2, testable = false)
   public static WebArchive createDeployment1() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-webservicerefsec-servlet-client.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.samples.webservicerefsec.Client.class)
               .addClass(org.jboss.test.ws.jaxws.samples.webservicerefsec.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.samples.webservicerefsec.EndpointService.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/webservicerefsec/WEB-INF/jboss-web.xml"), "jboss-web.xml")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/webservicerefsec/WEB-INF/wsdl/Endpoint.wsdl"), "wsdl/Endpoint.wsdl")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/webservicerefsec/WEB-INF/permissions.xml"), "permissions.xml")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/webservicerefsec/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-samples-webservicerefsec-servlet-client")
   public void testServletClient() throws Exception
   {
      URL url = new URL("http://" + baseURL.getHost() + ":" + baseURL.getPort() + "/jaxws-samples-webservicerefsec-servlet-client?echo=HelloWorld");
      assertEquals("HelloWorld", IOUtils.readAndCloseStream(url.openStream()));
   }
}
