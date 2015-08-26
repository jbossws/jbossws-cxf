/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws3870;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-3870] Imported schemas in weird places in deployments causes issues
 *
 * @author alessio.soldano@jboss.com
 * @since 19-Jul-2015
 */
@RunWith(Arquillian.class)
public class JBWS3870TestCase extends JBossWSTest
{
   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws3870.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws3870.SayHi.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3870.SayHiImpl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3870.sayhi1.SayHi.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3870.sayhi1.SayHiResponse.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3870.sayhi2.SayHiArray.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3870.sayhi2.SayHiArrayResponse.class)
               .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3870/wsdl/thewsdl/sayHi.wsdl"), "wsdl/thewsdl/sayHi.wsdl")
               .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3870/wsdl/sayhi/a.wsdl"), "wsdl/sayhi/a.wsdl")
               .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3870/wsdl/sayhi/sayhi-schema1.xsd"), "wsdl/sayhi/sayhi-schema1.xsd")
               .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3870/wsdl/sayhi/sayhi/a.wsdl"), "wsdl/sayhi/sayhi/a.wsdl")
               .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3870/wsdl/sayhi/sayhi/sayhi-schema1.xsd"), "wsdl/sayhi/sayhi/sayhi-schema1.xsd");
     return archive;
   }

   @Test
   @RunAsClient
   public void testService() throws Exception
   {
      String endpointAddress = "http://" + getServerHost() + ":" + getServerPort() + "/jaxws-jbws3870/SayHiImpl";
      Service service = Service.create(new URL(endpointAddress + "?wsdl"), new QName("http://apache.org/sayHi", "SayHiService"));
      SayHi port = service.getPort(new QName("http://apache.org/sayHi", "SayHiPort"), SayHi.class);
      assertEquals("Hi", port.sayHi("Foo"));
      assertEquals("Hi", port.sayHiArray(new ArrayList<String>()).iterator().next());
   }

}
