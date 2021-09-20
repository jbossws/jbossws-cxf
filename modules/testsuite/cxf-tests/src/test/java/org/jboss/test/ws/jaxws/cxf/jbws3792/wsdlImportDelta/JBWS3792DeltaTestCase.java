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
package org.jboss.test.ws.jaxws.cxf.jbws3792.wsdlImportDelta;

import java.io.File;
import java.net.URL;
import java.util.Iterator;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test imported wsdl is NOT co-located with the importing wsdl
 * and schema is NOT co-located with the referencing imported wsdl.
 *  
 * @author rsearls@redhat.com
 */
@RunWith(Arquillian.class)
public class JBWS3792DeltaTestCase extends JBossWSTest {

   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jbws3792-wsdlImportDelta.war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
            + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client\n"))
         .addClass(org.jboss.test.ws.jaxws.cxf.jbws3792.wsdlImportDelta.GreetingsWsImpl.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.jbws3792.wsdlImportDelta.GreetingsWs.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3792/wsdlImportDelta/WEB-INF/wsdl/Greeting_Simplest.wsdl"), "wsdl/Greeting_Simplest.wsdl")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3792/wsdlImportDelta/WEB-INF/wsdl/A/b/Hello.wsdl"), "wsdl/A/b/Hello.wsdl")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3792/wsdlImportDelta/WEB-INF/schema/Hello_schema2.xsd"), "schema/Hello_schema2.xsd");
      return archive;
   }

   @Test
   @RunAsClient
   public void testImportSimplest() throws Exception
   {

      URL wsdlURL = new URL(baseURL + "/GreetingsService?wsdl");
      QName qname = new QName("http://hello/test", "HelloService");
      Service service = Service.create(wsdlURL, qname);
      Iterator<QName> it = service.getPorts();
      int cnt = 0;
      while (it.hasNext())
      {
         cnt++;
         QName qn = (QName) it.next();
         assertTrue("{http://hello/test}HelloServicePort".equals(qn.toString()));
      }
      assertTrue("Expected cnt to be 1 but cnt is " + cnt, cnt == 1);
   }
}
