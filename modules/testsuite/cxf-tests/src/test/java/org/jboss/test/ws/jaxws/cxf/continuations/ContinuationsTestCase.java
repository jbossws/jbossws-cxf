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
package org.jboss.test.ws.jaxws.cxf.continuations;

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
 * Asynchronous web services test case with endpoint impl making
 * direct use of Apache CXF continuations api
 * 
 * @author alessio.soldano@jboss.com
 * @since 21-Jun-2012
 */
@RunWith(Arquillian.class)
public class ContinuationsTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-cxf-continuations.war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.apache.cxf\n"))
            .addClass(org.jboss.test.ws.jaxws.cxf.continuations.EndpointImpl.class)
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/continuations/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testAsyncEndpoint() throws Exception
   {
      QName serviceName = new QName("http://org.jboss.ws/cxf/continuations", "EndpointImplService");
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
      QName serviceName = new QName("http://org.jboss.ws/cxf/continuations", "EndpointImplService");
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
