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
package org.jboss.test.ws.jaxws.cxf.asyncclient;

import java.io.File;
import java.net.URL;
import java.util.Map;

import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
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
 * @author <a href="mailto:ema@redhat.com">Jim Ma</a>
 *
 */
@RunWith(Arquillian.class)
public class AsyncClientTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-cxf-asyncclient.war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.apache.cxf\n"))
            .addClass(org.jboss.test.ws.jaxws.cxf.asyncclient.EndpointImpl.class)
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/asyncclient/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testAsycClienWithHCAddress() throws Exception
   {
      if (baseURL.getHost().startsWith("[")) {
         System.out.println("FIXME: [CXF-6350] Can't turn on async transport by specifying endpoint address in JAX-WS client when using IPv6");
         return;
      }
      Endpoint proxy = initPort();
      BindingProvider provider = (BindingProvider)proxy;
      Map<String, Object> requestContext = provider.getRequestContext();
      requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "hc://" + baseURL);
      assertEquals("Echo:1000", proxy.echo(1000));
   }
   
   
   @Test
   @RunAsClient
   public void testAsycClienWithMsgProp() throws Exception
   {
      Endpoint proxy = initPort();
      BindingProvider provider = (BindingProvider)proxy;
      Map<String, Object> requestContext = provider.getRequestContext();
      requestContext.put("use.async.http.conduit", Boolean.TRUE);
      assertEquals("Echo:1000", proxy.echo(1000));
   }
   
   @Test
   @RunAsClient
   public void testAsycClienAsyncOperation() throws Exception
   {
      Endpoint proxy = initPort();
      BindingProvider provider = (BindingProvider)proxy;
      Map<String, Object> requestContext = provider.getRequestContext();
      requestContext.put("use.async.http.conduit", Boolean.TRUE);
      assertEquals("Echo:1000", proxy.echoAsync(1000).get());
   }
   
   @Test
   @RunAsClient
   public void testAysncClientWithPolicy () throws Exception 
   {
      Bus bus = BusFactory.newInstance().createBus();
      AsyncEnabledInfoInterceptor asyncInfo = new AsyncEnabledInfoInterceptor();
      try
      {
         bus.setProperty("use.async.http.conduit", "ASYNC_ONLY");
         bus.getOutInterceptors().add(asyncInfo);
         BusFactory.setThreadDefaultBus(bus);
         Endpoint proxy = initPort();
         assertEquals("Echo:1000", proxy.echo(1000));
         assertFalse("Async client is expected disabled", asyncInfo.isAsyncEnabled());
         assertEquals("Echo:1000", proxy.echoAsync(1000).get());
         assertTrue("Async client is expected enabled", asyncInfo.isAsyncEnabled());
      }
      finally
      {
         bus.shutdown(true);
      }
   }

   private Endpoint initPort() throws Exception {
      QName serviceName = new QName("http://org.jboss.ws/cxf/asyncclient", "EndpointImplService");
      URL wsdlURL = new URL(baseURL + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      Endpoint proxy = service.getPort(Endpoint.class);
      return proxy;
   }
}