/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.asyncclient;

import java.io.File;
import java.net.URL;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

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