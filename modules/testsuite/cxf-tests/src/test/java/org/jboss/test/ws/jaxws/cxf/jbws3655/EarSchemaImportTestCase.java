/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.jbws3655;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class EarSchemaImportTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static JavaArchive createDeployment3() {
      JavaArchive archive1 = ShrinkWrap.create(JavaArchive.class, "jaxws-cxf-jbws3655-ejb.jar");
      archive1.addManifest().addClass(org.jboss.test.ws.jaxws.cxf.jbws3655.HelloWSEJBImpl.class);
      writeToDisk(archive1);
      
      JavaArchive archive2 = ShrinkWrap.create(JavaArchive.class, "jaxws-cxf-jbws3655-jaxws.jar");
      archive2.addManifest()
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3655.HelloRequest.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3655.HelloResponse.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3655.HelloWs.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3655.HelloWsImpl.class)
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3655/META-INF/wsdl/Hello.wsdl"), "wsdl/Hello.wsdl")
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3655/META-INF/wsdl/Hello_schema1.xsd"), "wsdl/Hello_schema1.xsd");
      writeToDisk(archive2);
      
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-cxf-jbws3655.ear");
      archive.addManifest()
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3655/META-INF/application.xml"))
            .addAsResource(new File(JBossWSTestHelper.getTestArchiveDir(), "jaxws-cxf-jbws3655-jaxws.jar"), "lib/jaxws-cxf-jbws3655-jaxws.jar")
            .addAsResource(new File(JBossWSTestHelper.getTestArchiveDir(), "jaxws-cxf-jbws3655-ejb.jar"));
      return archive;
   }
   
   public static void writeToDisk(JavaArchive archive)
   {
      File file = new File(JBossWSTestHelper.getTestArchiveDir(), archive.getName());
      archive.as(ZipExporter.class).exportTo(file, true);
   }

   @Test
   @RunAsClient
   public void testSchemaImport() throws Exception
   {
      HelloWs port = getPort(baseURL + "/jaxws-cxf-jbws3655/HelloService");
      HelloRequest request = new HelloRequest();
      request.setInput("hello");
      HelloResponse response = port.doHello(request);
      assertEquals(2, response.getMultiHello().size());

   }

   private HelloWs getPort(String publishURL) throws Exception
   {
      URL wsdlURL = new URL(publishURL + "?wsdl");
      QName qname = new QName("http://hello/test", "HelloService");
      Service service = Service.create(wsdlURL, qname);
      return (HelloWs)service.getPort(HelloWs.class);
   }
}
