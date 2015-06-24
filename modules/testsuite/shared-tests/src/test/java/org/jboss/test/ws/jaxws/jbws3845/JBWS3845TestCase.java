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
package org.jboss.test.ws.jaxws.jbws3845;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.IgnoreContainer;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-3845] Injection not working in JAX-WS handlers from predefined configurations
 *
 * @author alessio.soldano@jboss.com
 * @since 03-Mar-2015
 */
@RunWith(Arquillian.class)
public class JBWS3845TestCase extends JBossWSTest
{
   @Rule
   public IgnoreContainer rule = new IgnoreContainer("[JBWS-3845] Injection not working in JAX-WS handlers from predefined configurations", "wildfly800");
   
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws3845.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws3845.MyBean.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3845.ServiceImpl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3845.ServiceInterface.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3845.CDIHandler.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3845/WEB-INF/beans.xml"), "beans.xml")
               .addAsResource("org/jboss/test/ws/jaxws/jbws3845/jaxws-endpoint-config.xml", "jaxws-endpoint-config.xml")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3845/WEB-INF/web.xml"));
     return archive;
   }

   @Test
   @RunAsClient
   public void testService() throws Exception
   {
      Service service = Service.create(new URL(baseURL + "/service?wsdl"), new QName("http://org.jboss.ws/jaxws/jbws3845/", "MyService"));
      ServiceInterface port = service.getPort(ServiceInterface.class);
      assertEquals("Greetings Mr. John", port.greetMe("John"));
   }

}
