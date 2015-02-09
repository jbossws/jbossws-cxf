/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws1529;

import java.io.File;
import java.net.URL;

import javax.wsdl.Definition;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

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
 * wsdlReader fails with faults defined on jaxws SEI
 * 
 * http://jira.jboss.org/jira/browse/JBWS-1529
 *
 * @author Thomas.Diesler@jboss.com
 */
@RunWith(Arquillian.class)
public class JBWS1529TestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws1529.war");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws1529.JBWS1529.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1529.JBWS1529Impl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1529.UserException.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1529/WEB-INF/web.xml"));
      return archive;
   }

   private JBWS1529 proxy;

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      QName serviceName = new QName("http://jbws1529.jaxws.ws.test.jboss.org/", "JBWS1529Service");
      URL wsdlURL = new URL(baseURL + "/TestService?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      proxy = (JBWS1529)service.getPort(JBWS1529.class);
   }

   @Override
   protected void tearDown() throws Exception
   {
      proxy = null;
      super.tearDown();
   }

   @Test
   @RunAsClient
   public void testWSDLReader() throws Exception
   {
      setUp();
      File wsdlFile = getResourceFile("jaxws/jbws1529/META-INF/wsdl/JBWS1529Service.wsdl");
      assertTrue(wsdlFile.exists());
      
      WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
      Definition wsdl = wsdlReader.readWSDL(wsdlFile.getAbsolutePath());
      assertNotNull(wsdl);
      tearDown();
   }

   @Test
   @RunAsClient
   public void testEcho() throws Exception
   {
      setUp();
      String retStr = proxy.echo("hi there");
      assertEquals("hi there", retStr);
      tearDown();
   }
}
