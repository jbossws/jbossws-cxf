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
package org.jboss.test.ws.jaxws.samples.asynchronous;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import jakarta.xml.ws.AsyncHandler;
import jakarta.xml.ws.Dispatch;
import jakarta.xml.ws.Response;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.Service.Mode;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.DOMUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Element;

/**
 * Test JAXWS asynchrous proxy
 *
 * @author Thomas.Diesler@jboss.org
 * @author alessio.soldano@jboss.com
 * @since 12-Aug-2006
 */
@RunWith(Arquillian.class)
public class AsynchronousTestCase extends JBossWSTest
{
   private final String targetNS = "http://org.jboss.ws/jaxws/asynchronous";
   private Exception handlerProxyException;
   private boolean asyncHandlerProxyCalled;
   
   private final String reqPayload = "<ns2:echo xmlns:ns2='" + targetNS + "'><String_1>Hello</String_1></ns2:echo>";
   private Exception handlerDispatchException;
   private boolean asyncHandlerDispatchCalled;

   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-asynchronous.war");
      archive
         .addManifest()
         .addClass(org.jboss.test.ws.jaxws.samples.asynchronous.Endpoint.class)
         .addClass(org.jboss.test.ws.jaxws.samples.asynchronous.EndpointBean.class)
         .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/asynchronous/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testInvokeSync() throws Exception
   {
      Endpoint port = createProxy();
      String retStr = port.echo("Hello");
      assertEquals("Hello", retStr);
   }

   @Test
   @RunAsClient
   public void testInvokeAsync() throws Exception
   {
      Endpoint port = createProxy();
      Response<String> response = port.echoAsync("Async");

      // access future
      String retStr = response.get();
      assertEquals("Async", retStr);
   }

   @Test
   @RunAsClient
   public void testInvokeAsyncHandler() throws Exception
   {
      AsyncHandler<String> handler = new AsyncHandler<String>()
      {
         @Override
         public void handleResponse(Response<String> response)
         {
            try
            {
               System.out.println("AsyncHandler.handleResponse() method called");
               String retStr = response.get(5000, TimeUnit.MILLISECONDS);
               assertEquals("Hello", retStr);
               asyncHandlerProxyCalled = true;
            }
            catch (Exception ex)
            {
               handlerProxyException = ex;
            }
         }
      };

      Endpoint port = createProxy();
      Future<?> future = port.echoAsync("Hello", handler);
      long start = System.currentTimeMillis();
      future.get(5000, TimeUnit.MILLISECONDS);
      long end = System.currentTimeMillis();
      System.out.println("Time spent in future.get() was " + (end - start) + "milliseconds");

      if (handlerProxyException != null)
         throw handlerProxyException;

      assertTrue("Async handler called", asyncHandlerProxyCalled);
   }

   private Endpoint createProxy() throws MalformedURLException
   {
      URL wsdlURL = new URL(baseURL + "?wsdl");
      QName serviceName = new QName(targetNS, "EndpointBeanService");
      Service service = Service.create(wsdlURL, serviceName);
      return service.getPort(Endpoint.class);
   }
   
   @Test
   @RunAsClient
   public void testInvokeAsyncDispatch() throws Exception
   {
      Source reqObj = new StreamSource(new StringReader(reqPayload));
      Response<Source> response = createDispatch().invokeAsync(reqObj);
      verifyResponse(response.get(3000, TimeUnit.MILLISECONDS));
   }

   @Test
   @RunAsClient
   public void testInvokeAsyncDispatchHandler() throws Exception
   {
      AsyncHandler<Source> handler = new AsyncHandler<Source>()
      {
         @Override
         public void handleResponse(Response<Source> response)
         {
            try
            {
               verifyResponse(response.get());
               asyncHandlerDispatchCalled = true;
            }
            catch (Exception ex)
            {
               handlerDispatchException = ex;
            }
         }
      };
      StreamSource reqObj = new StreamSource(new StringReader(reqPayload));
      Future<?> future = createDispatch().invokeAsync(reqObj, handler);
      future.get(1000, TimeUnit.MILLISECONDS);

      if (handlerDispatchException != null)
         throw handlerDispatchException;

      assertTrue("Async handler called", asyncHandlerDispatchCalled);
   }

   private Dispatch<Source> createDispatch() throws MalformedURLException
   {
      URL wsdlURL = new URL(baseURL + "?wsdl");
      QName serviceName = new QName(targetNS, "EndpointBeanService");
      QName portName = new QName(targetNS, "EndpointPort");
      Service service = Service.create(wsdlURL, serviceName);
      return service.createDispatch(portName, Source.class, Mode.PAYLOAD);
   }

   private void verifyResponse(Source result) throws IOException
   {
      Element docElement = DOMUtils.sourceToElement(result);
      Element retElement = DOMUtils.getFirstChildElement(docElement);
      assertEquals("result", retElement.getNodeName());
      assertEquals("Hello", retElement.getTextContent());
   }
}
