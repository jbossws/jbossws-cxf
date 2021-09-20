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
package org.jboss.test.ws.jaxws.jbws3034;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

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

@RunWith(Arquillian.class)
public class JBWS3034TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws3034.war");
         archive
               .addManifest()
               .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3034/handlers.xml"), "handlers.xml")
               .addClass(org.jboss.test.ws.jaxws.jbws3034.Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3034.EndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3034.ServerSOAPHandler.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3034/WEB-INF/jboss-web.xml"), "jboss-web.xml")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3034/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testCall() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "?wsdl");
      Service service = Service.create(wsdlURL, new QName("http://ws.jboss.org/jbws3034", "EndpointService"));
      Endpoint port = service.getPort(new QName("http://ws.jboss.org/jbws3034", "EndpointPort"), Endpoint.class);
      String response = port.echo("testJBWS3034");
      assertEquals("PutByServerSOAPHandler", response);
   }
}