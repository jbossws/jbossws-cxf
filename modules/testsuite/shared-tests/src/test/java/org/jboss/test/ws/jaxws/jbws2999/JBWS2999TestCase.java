/*
* JBoss, Home of Professional Open Source.
* Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws2999;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

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
 * [JBWS-2999] cxf webservices.xml override with jaxws
 *
 * @author alessio.soldano@jboss.com
 * @since 15-Apr-2010
 */
@RunWith(Arquillian.class)
public class JBWS2999TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static JavaArchive createDeployments() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws2999.jar");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws2999.CustomHandler.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2999.Hello.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2999.HelloBean.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2999/META-INF/ejb-jar.xml"), "ejb-jar.xml")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2999/META-INF/webservices.xml"), "webservices.xml")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2999/META-INF/wsdl/HelloService.wsdl"), "wsdl/HelloService.wsdl");
      return archive;
   }

   private Hello getPort() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/jaxws-jbws2999/JunkServiceName/HelloBean?wsdl");
      QName serviceName = new QName("http://Hello.org", "HelloService");

      Service service = Service.create(wsdlURL, serviceName);
      return service.getPort(Hello.class);
   }

   @Test
   @RunAsClient
   public void testCall() throws Exception
   {
      String message = "Hello";
      String response = getPort().helloEcho(message);
      assertEquals(message + "World", response);
   }
}
