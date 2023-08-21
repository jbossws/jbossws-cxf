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
package org.jboss.test.ws.jaxws.cxf.interceptors;

import java.io.File;
import java.net.URL;

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
 * Testcase for:
 * [JBWS-3837] Apache CXF interceptors setup through properties
 * [JBWS-3840] jboss-webservices.xml support for Apache CXF interceptor properties  
 *
 * @author alessio.soldano@jboss.com
 * @since 10-Oct-2014
 */
@RunWith(Arquillian.class)
public class InterceptorsTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-cxf-interceptors.war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.apache.cxf\n"))
            .addClass(org.jboss.test.ws.jaxws.cxf.interceptors.EndpointImpl.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.interceptors.AnotherEndpointImpl.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.interceptors.BusInterceptor.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.interceptors.BusCounterInterceptor.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.interceptors.DeclaredInterceptor.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.interceptors.EndpointInterceptor.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.interceptors.EndpointCounterInterceptor.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.interceptors.JBossWSFaultListener.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.interceptors.Counter.class)
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/interceptors/WEB-INF/jboss-webservices.xml"), "jboss-webservices.xml")
            .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/interceptors/WEB-INF/jaxws-endpoint-config.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testEndpointWithBothBusAndEndpointInterceptors() throws Exception {
      URL wsdlURL = new URL(baseURL + "MyService?wsdl");
      Service service = Service.create(wsdlURL, new QName("http://org.jboss.ws.jaxws.cxf/interceptors", "MyService"));
      Endpoint port = service.getPort(new QName("http://org.jboss.ws.jaxws.cxf/interceptors", "MyEndpointPort"), Endpoint.class);
      assertEquals("Hi FooBar! 0", port.echo("Hi"));
      assertEquals("Hi FooBar! 2", port.echo("Hi"));
      assertEquals("Hi FooBar! 4", port.echo("Hi"));
      assertEquals("Hi FooBar! 6", port.echo("Hi"));
   }
   
   @Test
   @RunAsClient
   public void testEndpointWithBusInterceptorsOnly() throws Exception {
      URL wsdlURL = new URL(baseURL + "AnotherService?wsdl");
      Service service = Service.create(wsdlURL, new QName("http://org.jboss.ws.jaxws.cxf/interceptors", "AnotherService"));
      AnotherEndpoint port = service.getPort(new QName("http://org.jboss.ws.jaxws.cxf/interceptors", "AnotherEndpointPort"), AnotherEndpoint.class);
      assertEquals("Hi.Foo!.0", port.echo("Hi"));
      assertEquals("Hi.Foo!.1", port.echo("Hi"));
      assertEquals("Hi.Foo!.2", port.echo("Hi"));
      assertEquals("Hi.Foo!.3", port.echo("Hi"));
   }
   
   @Test
   @RunAsClient
   public void testFaultListner() throws Exception {
      URL wsdlURL = new URL(baseURL + "MyService?wsdl");
      Service service = Service.create(wsdlURL, new QName("http://org.jboss.ws.jaxws.cxf/interceptors", "MyService"));
      Endpoint port = service.getPort(new QName("http://org.jboss.ws.jaxws.cxf/interceptors", "MyEndpointPort"), Endpoint.class);
      try
      {
         port.echoException("hi");
      }
      catch (Exception e)
      {
         //expect a runtime exception;
      }
      assertTrue("FaultListener is not set propertly",
            port.getException().contains("{http://org.jboss.ws.jaxws.cxf/interceptors}MyService#{http://org.jboss.ws.jaxws.cxf/interceptors}echoException")
                  && port.getException().contains("Intended Exception"));
   }
}
