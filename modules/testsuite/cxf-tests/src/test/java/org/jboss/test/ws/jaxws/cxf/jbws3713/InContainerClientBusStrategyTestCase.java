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
package org.jboss.test.ws.jaxws.cxf.jbws3713;

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
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.stack.cxf.client.Constants;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class InContainerClientBusStrategyTestCase extends JBossWSTest
{
   private static final String DEP = "jaxws-cxf-jbws3713-ict";
   private static final String CLIENT_DEP = "jaxws-cxf-jbws3713-client";

   @ArquillianResource
   private URL baseURL;
   
   @Deployment(name = DEP, testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, DEP + ".war");
      archive.addManifest()
         .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.HelloRequest.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.HelloResponse.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.HelloWSImpl.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.HelloWs.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3713/WEB-INF/wsdl/Hello.wsdl"), "wsdl/Hello.wsdl")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3713/WEB-INF/wsdl/Hello_schema1.xsd"), "wsdl/Hello_schema1.xsd")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3713/WEB-INF/wsdl/Hello_schema2.xsd"), "wsdl/Hello_schema2.xsd")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3713/WEB-INF/wsdl/Hello_schema3.xsd"), "wsdl/Hello_schema3.xsd")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3713/WEB-INF/wsdl/Hello_schema4.xsd"), "wsdl/Hello_schema4.xsd")
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3713/WEB-INF/wsdl/Hello_schema5.xsd"), "wsdl/Hello_schema5.xsd");
      return archive;
   }
   
   @Deployment(name = CLIENT_DEP, testable = false)
   public static WebArchive createDeployment2() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, CLIENT_DEP + ".war");
      archive
         .setManifest(new StringAsset("Manifest-Version: 1.0\n"
               + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client,org.apache.cxf.impl\n"))
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3713/WEB-INF/permissions.xml"), "permissions.xml")
         .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.BusCounter.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.ClientServlet.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.ClientServletUsignThreadLocal.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.HelloRequest.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.HelloResponse.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.HelloWs.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.Helper.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.jbws3713.HelperUsignThreadLocal.class);
      return archive;
   }

   @Test
   @RunAsClient
   @OperateOnDeployment(DEP)
   public void testEndpoint() throws Exception
   {
      HelloWs port = getPort(baseURL + "/HelloService");
      HelloRequest request = new HelloRequest();
      request.setInput("hello");
      HelloResponse response = port.doHello(request);
      assertEquals(2, response.getMultiHello().size());
      assertTrue(response.getMultiHello().contains("hello"));
      assertTrue(response.getMultiHello().contains("world"));
   }
   
   @Test
   @RunAsClient
   @OperateOnDeployment(CLIENT_DEP)
   public void testClientWithNewBusStrategy() throws Exception
   {
      final int threadPoolSize = 10;
      final int invocations = 50;
      int busCount = callServlet("client-using-threadlocal", Constants.NEW_BUS_STRATEGY, threadPoolSize, invocations);
      assertEquals(threadPoolSize, busCount);
      
      busCount = callServlet("client", Constants.NEW_BUS_STRATEGY, threadPoolSize, invocations);
      assertEquals(invocations, busCount);
   }
   
   @Test
   @RunAsClient
   @OperateOnDeployment(CLIENT_DEP)
   public void testClientWithThreadBusStrategy() throws Exception
   {
      final int threadPoolSize = 10;
      final int invocations = 50;
      int busCount = callServlet("client-using-threadlocal", Constants.THREAD_BUS_STRATEGY, threadPoolSize, invocations);
      assertEquals(threadPoolSize, busCount);
      
      busCount = callServlet("client", Constants.THREAD_BUS_STRATEGY, threadPoolSize, invocations);
      assertEquals(threadPoolSize, busCount);
   }
   
   @Test
   @RunAsClient
   @OperateOnDeployment(CLIENT_DEP)
   public void testClientWithTCCLBusStrategy() throws Exception
   {
      final int threadPoolSize = 10;
      final int invocations = 50;
      int busCount = callServlet("client-using-threadlocal", Constants.TCCL_BUS_STRATEGY, threadPoolSize, invocations);
      assertEquals(1, busCount);
      
      busCount = callServlet("client", Constants.TCCL_BUS_STRATEGY, threadPoolSize, invocations);
      assertEquals(1, busCount);
   }
   
   private int callServlet(String pattern, String strategy, int threads, int calls) throws Exception {
      URL url = new URL(baseURL + pattern + "?strategy="
            + strategy + "&path=/jaxws-cxf-jbws3713-ict/HelloService&threads=" + threads + "&calls=" + calls);
      return Integer.parseInt(IOUtils.readAndCloseStream(url.openStream()));
   }

   private HelloWs getPort(String publishURL) throws Exception
   {
      URL wsdlURL = new URL(publishURL + "?wsdl");
      QName qname = new QName("http://hello/test", "HelloService");
      Service service = Service.create(wsdlURL, qname);
      return service.getPort(HelloWs.class);
   }
}
