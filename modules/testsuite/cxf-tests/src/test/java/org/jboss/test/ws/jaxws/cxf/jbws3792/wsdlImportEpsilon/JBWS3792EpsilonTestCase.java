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
package org.jboss.test.ws.jaxws.cxf.jbws3792.wsdlImportEpsilon;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.ws.jaxws.cxf.jbws3792.HelloRequest;
import org.jboss.test.ws.jaxws.cxf.jbws3792.HelloResponse;
import org.jboss.test.ws.jaxws.cxf.jbws3792.HelloWs;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test imported wsdl identified by URL of deployed app.
 * 
 * @author rsearls@redhat.com
 */
@RunWith(Arquillian.class)
public class JBWS3792EpsilonTestCase extends JBossWSTest {
   
   private static final String DEP1 = "jbws3792-hello";
   private static final String DEP2 = "jbws3792-wsdlImportEpsilon";
   
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(name = DEP1, testable = false, order = 1)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, DEP1 + ".war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
           + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client\n"))
         .addClass(org.jboss.test.ws.jaxws.cxf.jbws3792.HelloResponse.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.jbws3792.HelloRequest.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.jbws3792.HelloWSImpl.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.jbws3792.HelloWs.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3792/WEB-INF/wsdl/Hello.wsdl"), "wsdl/Hello.wsdl")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3792/WEB-INF/wsdl/Hello_schema2.xsd"), "wsdl/Hello_schema2.xsd");
      return archive;
   }

   @Deployment(name = DEP2, testable = false, order = 2)
   public static WebArchive createDeployment2() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, DEP2 + ".war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
           + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client\n"))
         .addClass(org.jboss.test.ws.jaxws.cxf.jbws3792.wsdlImportEpsilon.GreetingsWsImpl.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.jbws3792.wsdlImportEpsilon.GreetingsWs.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3792/wsdlImportEpsilon/WEB-INF/wsdl/Greeting_Simplest.wsdl"), "wsdl/Greeting_Simplest.wsdl");
      return archive;
   }

   @Test
   @RunAsClient
   @OperateOnDeployment(DEP2)
   public void testImportSimplest() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/GreetingsService?wsdl");
      QName qname = new QName("http://hello/test", "HelloService");
      Service service = Service.create(wsdlURL, qname);
      HelloWs hello = (HelloWs) service.getPort(HelloWs.class);
      HelloRequest hReq = new HelloRequest();
      hReq.setInput("Joe");
      HelloResponse hRep = hello.doHello(hReq);
      assertEquals("Joe", hRep.getMultiHello().get(0));
   }
}
