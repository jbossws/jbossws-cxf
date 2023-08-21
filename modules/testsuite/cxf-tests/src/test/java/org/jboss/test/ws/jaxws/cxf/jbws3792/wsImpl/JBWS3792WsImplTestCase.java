/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.test.ws.jaxws.cxf.jbws3792.wsImpl;

import java.io.File;
import java.net.URL;
import java.util.Iterator;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

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
