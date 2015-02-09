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
package org.jboss.test.ws.jaxws.cxf.jbws3713;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

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
