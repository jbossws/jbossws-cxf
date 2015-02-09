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
package org.jboss.test.ws.jaxws.cxf.jbws3792.wsImpl;

import java.io.File;
import java.net.URL;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test that a external wsdl declared by a valid URL in a wsdlLocation
 * property of a WebService annotation is supported.
 * 
 * @author rsearls@redhat.com
 */
@RunWith(Arquillian.class)
public class JBWS3792WsImplTestCase extends JBossWSTest {

   private static final String DEP_EXT = "jbws3792-external-wsdl";
   private static final String DEP = "jbws3792-ws-impl";
   
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(name = DEP_EXT, testable = false, order = 1)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, DEP_EXT + ".war");
      archive
         .addClass(org.jboss.test.ws.jaxws.cxf.jbws3792.externalWsdl.JBWS3792WS.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3792/externalWsdl/WEB-INF/web.xml"), "web.xml")
         .add(new FileAsset(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3792/externalWsdl/WEB-INF/wsdl/jbws3792.wsdl")), ArchivePaths.root().get() + "jbws3792.wsdl")
         .add(new FileAsset(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3792/externalWsdl/WEB-INF/wsdl/import.wsdl")), "/import.wsdl");
      return archive;
   }

   @Deployment(name = DEP, testable = false, order = 2)
   public static WebArchive createDeployment2() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, DEP + ".war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
            + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client\n"))
         .addClass(org.jboss.test.ws.jaxws.cxf.jbws3792.wsImpl.Hello.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.jbws3792.wsImpl.HelloResponse.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.jbws3792.wsImpl.JBWS3792WS.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.jbws3792.wsImpl.JBWS3792WSImpl.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.jbws3792.wsImpl.JBWS3792WSService.class)
         .addAsWebInfResource(new FileAsset(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3792/wsImpl/WEB-INF/webservices.xml")), "webservices.xml")
         .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3792/wsImpl/WEB-INF/web.xml"));
      return archive;
   }


   @Test
   @RunAsClient
   @OperateOnDeployment(DEP)
   public void test() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/JBWS3792WSService?wsdl");
      QName qname = new QName("http://test.jbws3792/", "JBWS3792WSService");
      Service service = Service.create(wsdlURL, qname);

      Iterator<QName> it = service.getPorts();
      int cnt = 0;
      while (it.hasNext()) {
         cnt++;
         QName qn = (QName)it.next();
         assertTrue("qname: " + qn.toString(), "{http://test.jbws3792/}JBWS3792WSPort".equals(qn.toString()));
      }
      assertTrue("Expected cnt to be 1 but cnt is " + cnt, cnt == 1);
   }

}
