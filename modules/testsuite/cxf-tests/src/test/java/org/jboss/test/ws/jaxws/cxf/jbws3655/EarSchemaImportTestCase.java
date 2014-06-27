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

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestSetup;

public class EarSchemaImportTestCase extends JBossWSTest
{
   public final String endpointAddress = "http://" + getServerHost() + ":8080/jaxws-cxf-jbws3655/HelloService";

   static {
      JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-cxf-jbws3655-ejb.jar") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.cxf.jbws3655.HelloWSEJBImpl.class);
         }
      });
      JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-cxf-jbws3655-jaxws.jar") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.cxf.jbws3655.HelloRequest.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.jbws3655.HelloResponse.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.jbws3655.HelloWs.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.jbws3655.HelloWsImpl.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3655/META-INF/wsdl/Hello.wsdl"), "wsdl/Hello.wsdl")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3655/META-INF/wsdl/Hello_schema1.xsd"), "wsdl/Hello_schema1.xsd");
         }
      });
      JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-cxf-jbws3655.ear") { {
         archive
               .addManifest()
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3655/META-INF/application.xml"))
               .addAsResource(new File(JBossWSTestHelper.getTestArchiveDir(), "jaxws-cxf-jbws3655-jaxws.jar"), "lib/jaxws-cxf-jbws3655-jaxws.jar")
               .addAsResource(new File(JBossWSTestHelper.getTestArchiveDir(), "jaxws-cxf-jbws3655-ejb.jar"));
         }
      });
   }

   public static Test suite()
   {
      return new JBossWSTestSetup(EarSchemaImportTestCase.class, "jaxws-cxf-jbws3655.ear");
   }

   public void testSchemaImport() throws Exception
   {
      HelloWs port = getPort(endpointAddress);
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
