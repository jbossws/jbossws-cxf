/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.jboss.test.ws.jaxws.jbws3276;

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

/**
 * [JBWS-3276] Tests anonymous POJO in web archive that contains web.xml with other endpoint.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
@RunWith(Arquillian.class)
public class Usecase1TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws3276-usecase1.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws3276.AnonymousPOJO.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3276.POJOIface.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3276.POJOImpl.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3276/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testAnonymousEndpoint() throws Exception
   {
      final QName serviceName = new QName("org.jboss.test.ws.jaxws.jbws3276", "AnonymousPOJOService");
      final URL wsdlURL = new URL(baseURL +  "/AnonymousPOJOService?wsdl");
      final Service service = Service.create(wsdlURL, serviceName);
      final POJOIface port = service.getPort(POJOIface.class);
      final String result = port.echo("hello");
      assertEquals("hello from anonymous POJO", result);
   }

   @Test
   @RunAsClient
   public void testDeclaredEndpoint() throws Exception
   {
      final QName serviceName = new QName("org.jboss.test.ws.jaxws.jbws3276", "POJOImplService");
      final URL wsdlURL = new URL(baseURL +  "/POJOService?wsdl");
      final Service service = Service.create(wsdlURL, serviceName);
      final POJOIface port = service.getPort(POJOIface.class);
      final String result = port.echo("hello");
      assertEquals("hello from POJO", result);
   }

}
