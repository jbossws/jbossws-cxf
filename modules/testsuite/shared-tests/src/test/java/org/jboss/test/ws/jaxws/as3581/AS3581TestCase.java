/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.jboss.test.ws.jaxws.as3581;

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
 * [AS7-3581] Tests manual JNDI lookup in @Oneway annotated method.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
@RunWith(Arquillian.class)
public class AS3581TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-as3581.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.as3581.EndpointIface.class)
               .addClass(org.jboss.test.ws.jaxws.as3581.EndpointIface2.class)
               .addClass(org.jboss.test.ws.jaxws.as3581.EndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.as3581.EndpointImpl2.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/as3581/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testEndpoint() throws Exception
   {
      // test one-way scenario
      final QName serviceName = new QName("org.jboss.test.ws.jaxws.as3581", "SimpleService");
      final URL wsdlURL = new URL(baseURL + "/SimpleService?wsdl");
      final Service service = Service.create(wsdlURL, serviceName);
      final EndpointIface port = service.getPort(EndpointIface.class);
      port.doit();
      // test req-resp scenario
      final QName serviceName2 = new QName("org.jboss.test.ws.jaxws.as3581", "SimpleService2");
      final URL wsdlURL2 = new URL(baseURL + "/SimpleService2?wsdl");
      final Service service2 = Service.create(wsdlURL2, serviceName2);
      final EndpointIface2 port2 = service2.getPort(EndpointIface2.class);
      final String oneWayLookupString = port2.getString();
      assertEquals("Ahoj", oneWayLookupString);
   }

}
