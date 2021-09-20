/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.asynch;

import java.io.File;
import java.net.URL;
import java.util.concurrent.Future;

import javax.xml.namespace.QName;
import jakarta.xml.ws.AsyncHandler;
import jakarta.xml.ws.Response;
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
 * Asynchronous web services test case (both client and server side async)
 * 
 * @author alessio.soldano@jboss.com
 * @since 21-Jun-2012
 */
@RunWith(Arquillian.class)
public class AsynchTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDep() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-asynch.war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
               + "Dependencies: org.apache.cxf\n"))
         .addClass(org.jboss.test.ws.jaxws.samples.asynch.EndpointImpl.class)
         .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/asynch/WEB-INF/web.xml"));
      return archive;
   }
   
   @Test
   @RunAsClient
   public void testEndpoint() throws Exception
   {
      QName serviceName = new QName("http://org.jboss.ws/cxf/samples/asynch", "EndpointImplService");
      URL wsdlURL = new URL(baseURL + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      Endpoint proxy = service.getPort(Endpoint.class);
      final String user = "Kermit";
      assertEquals(user + " (ASYNC)", proxy.echo(user)); //expect ASYNC as on server side the invocation is dispatched to the async method
   }

   @Test
   @RunAsClient
   public void testAsyncEndpoint() throws Exception
   {
      QName serviceName = new QName("http://org.jboss.ws/cxf/samples/asynch", "EndpointImplService");
      URL wsdlURL = new URL(baseURL + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      Endpoint proxy = service.getPort(Endpoint.class);
      final String user = "Kermit";
      //do something... then get the result
      assertEquals(user + " (ASYNC)", proxy.echoAsync(user).get());
   }
   
   @Test
   @RunAsClient
   public void testAsyncEndpointUsingHandler() throws Exception
   {
      QName serviceName = new QName("http://org.jboss.ws/cxf/samples/asynch", "EndpointImplService");
      URL wsdlURL = new URL(baseURL + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      Endpoint proxy = service.getPort(Endpoint.class);
      final String user = "Kermit";
      TestAsyncHandler handler = new TestAsyncHandler();
      Future<String> future = proxy.echoAsync(user, handler);
      //do something... then get the result when it's ready
      while (!future.isDone()) {
         Thread.sleep(100);
      }
      assertEquals(user + " (ASYNC)", handler.getResponse());
   }
   
   private class TestAsyncHandler implements AsyncHandler<String> {
      
      private String res;
      
      @Override
      public void handleResponse(Response<String> response) {
         try {
            res = response.get();
         } catch (Exception e) {
            e.printStackTrace();
            res = e.getMessage();
         }
      }
      
      public String getResponse() {
         return res;
      }
   }
}
