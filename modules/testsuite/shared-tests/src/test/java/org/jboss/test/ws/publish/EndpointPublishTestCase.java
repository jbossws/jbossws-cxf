/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.publish;

import java.io.File;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test WS endpoint publish api to JBoss AS container
 *
 * @author alessio.soldano@jboss.com
 * @since 13-Jul-2011
 */
@RunWith(Arquillian.class)
public class EndpointPublishTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class,"endpoint-publish.war");
         archive
            .setManifest(new StringAsset("Manifest-Version: 1.0\n"
               + "Dependencies: org.jboss.ws.common\n"))
            .addAsResource("org/jboss/test/ws/publish/WEB-INF/wsdl/EndpointImpl3.xml", "WEB-INF/wsdl/EndpointImpl3.xml")
            .addAsResource("org/jboss/test/ws/publish/WEB-INF/wsdl/EndpointImpl4.xml", "WEB-INF/wsdl/EndpointImpl4.xml")
            .addClass(org.jboss.test.ws.publish.Endpoint.class)
            .addClass(org.jboss.test.ws.publish.EndpointImpl.class)
            .addClass(org.jboss.test.ws.publish.EndpointImpl2.class)
            .addClass(org.jboss.test.ws.publish.EndpointImpl3.class)
            .addClass(org.jboss.test.ws.publish.EndpointImpl4.class)
            .addClass(org.jboss.test.ws.publish.EndpointImpl5.class)
            .addAsResource("org/jboss/test/ws/publish/EndpointImpl5.xml")
            .addClass(org.jboss.test.ws.publish.EndpointPublishServlet.class)
            .addAsResource("org/jboss/test/ws/publish/TestService.xml")
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/publish/META-INF/permissions.xml"), "permissions.xml")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/publish/WEB-INF/wsdl/EndpointImpl3.xml"), "wsdl/EndpointImpl3.xml");
      return archive;
   }

   @Test
   @RunAsClient
   public void testEndpointPublish() throws Exception
   {
      URL url = new URL(baseURL + "/endpoint-publish");
      assertEquals("1", IOUtils.readAndCloseStream(url.openStream()));
   }
}
