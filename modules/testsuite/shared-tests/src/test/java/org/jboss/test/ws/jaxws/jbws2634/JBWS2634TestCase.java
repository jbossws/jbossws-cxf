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
package org.jboss.test.ws.jaxws.jbws2634;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.ws.jaxws.jbws2634.webservice.EndpointIface;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-2634] Implement support for @EJB annotations in WS components
 * [JBWS-3845] Injection in JAX-WS handler from pre-defined configurations
 *
 * @author <a href="mailto:richard.opalka@jboss.org">Richard Opalka</a>
 * @author <a href="mailto:alessio.soldano@jboss.com">Alessio Soldano</a>
 */
@RunWith(Arquillian.class)
public final class JBWS2634TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static JavaArchive createDeployment() {
      WebArchive archive1 = ShrinkWrap.create(WebArchive.class, "jaxws-jbws2634-pojo.war");
         archive1
            .setManifest(new StringAsset("Manifest-Version: 1.0\n"
               + "Dependencies: org.jboss.logging\n"))
            .addClass(org.jboss.test.ws.jaxws.jbws2634.webservice.POJOBean.class)
            .addClass(org.jboss.test.ws.jaxws.jbws2634.webservice.POJOBean2.class)
            .addClass(org.jboss.test.ws.jaxws.jbws2634.webservice.POJOBean3.class)
            .addAsResource("org/jboss/test/ws/jaxws/jbws2634/webservice/jaxws-handler.xml")
            .addAsResource("org/jboss/test/ws/jaxws/jbws2634/webservice/jaxws-endpoint-config.xml", "jaxws-endpoint-config.xml")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2634/WEB-INF/web.xml"));
      JBossWSTestHelper.writeToFile(archive1);

      JavaArchive archive2 = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws2634.jar");
         archive2
            .addManifest()
            .addClass(org.jboss.test.ws.jaxws.jbws2634.shared.BeanIface.class)
            .addClass(org.jboss.test.ws.jaxws.jbws2634.shared.BeanImpl.class)
            .addClass(org.jboss.test.ws.jaxws.jbws2634.shared.handlers.TestHandler.class)
            .addClass(org.jboss.test.ws.jaxws.jbws2634.webservice.AbstractEndpointImpl.class)
            .addClass(org.jboss.test.ws.jaxws.jbws2634.webservice.EndpointIface.class);
      JBossWSTestHelper.writeToFile(archive2);

      JavaArchive archive3 = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws2634-ejb3.jar");
         archive3
            .setManifest(new StringAsset("Manifest-Version: 1.0\n"
               + "Dependencies: org.jboss.logging\n"))
            .addClass(org.jboss.test.ws.jaxws.jbws2634.webservice.EJB3Bean.class)
            .addClass(org.jboss.test.ws.jaxws.jbws2634.webservice.EJB3Bean2.class)
            .addClass(org.jboss.test.ws.jaxws.jbws2634.webservice.EJB3Bean3.class)
            .addAsResource("org/jboss/test/ws/jaxws/jbws2634/webservice/jaxws-handler.xml")
            .addAsResource("org/jboss/test/ws/jaxws/jbws2634/webservice/jaxws-endpoint-config.xml", "jaxws-endpoint-config.xml")
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2634/META-INF/ejb-jar.xml"), "ejb-jar.xml");
      JBossWSTestHelper.writeToFile(archive3);

      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-jbws2634.ear");
         archive
            .addManifest()
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2634/META-INF/application.xml"), "application.xml")
            .addAsResource(new File(JBossWSTestHelper.getTestArchiveDir(), "jaxws-jbws2634-pojo.war"))
            .addAsResource(new File(JBossWSTestHelper.getTestArchiveDir(), "jaxws-jbws2634-ejb3.jar"))
            .addAsResource(new File(JBossWSTestHelper.getTestArchiveDir(), "jaxws-jbws2634.jar"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testPojoEndpointInjection() throws Exception
   {
      QName serviceName = new QName("http://jbossws.org/JBWS2634", "POJOService");
      URL wsdlURL = new URL(baseURL + "/POJOService?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      EndpointIface proxy = (EndpointIface)service.getPort(EndpointIface.class);
      assertEquals("Hello World!:Inbound:TestHandler:POJOBean:Outbound:TestHandler", proxy.echo("Hello World!"));
   }

   @Test
   @RunAsClient
   @Ignore(value = "[JBWS-3846] Refactor creation process of jaxws handlers from predefined configurations")
   public void testPojoEndpoint2Injection() throws Exception
   {
      QName serviceName = new QName("http://jbossws.org/JBWS2634", "POJOService2");
      URL wsdlURL = new URL(baseURL + "/POJOService2?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      EndpointIface proxy = (EndpointIface)service.getPort(EndpointIface.class);
      assertEquals("Hello World!:Inbound:TestHandler:POJOBean2:Outbound:TestHandler", proxy.echo("Hello World!"));
   }

   @Test
   @RunAsClient
   @Ignore(value = "[JBWS-3846] Refactor creation process of jaxws handlers from predefined configurations")
   public void testPojoEndpoint3Injection() throws Exception
   {
      QName serviceName = new QName("http://jbossws.org/JBWS2634", "POJOService3");
      URL wsdlURL = new URL(baseURL + "/POJOService3?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      EndpointIface proxy = (EndpointIface)service.getPort(EndpointIface.class);
      assertEquals("Hello World!:Inbound:TestHandler:POJOBean3:Outbound:TestHandler", proxy.echo("Hello World!"));
   }

   @Test
   @RunAsClient
   public void testEjb3EndpointInjection() throws Exception
   {
      QName serviceName = new QName("http://jbossws.org/JBWS2634", "EJB3Service");
      URL wsdlURL = new URL("http://" + baseURL.getHost() + ":" + baseURL.getPort() + "/jaxws-jbws2634-ejb3/EJB3Service?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      EndpointIface proxy = (EndpointIface)service.getPort(EndpointIface.class);
      assertEquals("Hello World!:Inbound:TestHandler:EJB3Bean:Outbound:TestHandler", proxy.echo("Hello World!"));
   }

   @Test
   @RunAsClient
   @Ignore(value = "[JBWS-3846] Refactor creation process of jaxws handlers from predefined configurations")
   public void testEjb3Endpoint2Injection() throws Exception
   {
      QName serviceName = new QName("http://jbossws.org/JBWS2634", "EJB3Service2");
      URL wsdlURL = new URL("http://" + baseURL.getHost() + ":" + baseURL.getPort() + "/jaxws-jbws2634-ejb3/EJB3Service2?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      EndpointIface proxy = (EndpointIface)service.getPort(EndpointIface.class);
      assertEquals("Hello World!:Inbound:TestHandler:EJB3Bean2:Outbound:TestHandler", proxy.echo("Hello World!"));
   }

   @Test
   @RunAsClient
   @Ignore(value = "[JBWS-3846] Refactor creation process of jaxws handlers from predefined configurations")
   public void testEjb3Endpoint3Injection() throws Exception
   {
      QName serviceName = new QName("http://jbossws.org/JBWS2634", "EJB3Service3");
      URL wsdlURL = new URL("http://" + baseURL.getHost() + ":" + baseURL.getPort() + "/jaxws-jbws2634-ejb3/EJB3Service3?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      EndpointIface proxy = (EndpointIface)service.getPort(EndpointIface.class);
      assertEquals("Hello World!:Inbound:TestHandler:EJB3Bean3:Outbound:TestHandler", proxy.echo("Hello World!"));
   }
}
