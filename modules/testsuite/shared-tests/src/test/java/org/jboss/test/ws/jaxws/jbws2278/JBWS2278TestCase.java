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
package org.jboss.test.ws.jaxws.jbws2278;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.handler.Handler;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filter;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-2278] JBossWS is picking the wrong binding when both Soap1.1 and Soap1.2 bindings are provided for a port
 *
 * @author alessio.soldano@jboss.com
 * @since 30-Sep-2008
 * @see https://jira.jboss.org/jira/browse/JBWS-2278
 */
@RunWith(Arquillian.class)
public class JBWS2278TestCase extends JBossWSTest
{
   private static TestEndpoint port11;
   private static TestEndpoint port12;

   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static WebArchive createDeployments() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-jbws2278.war");
         archive
            .addManifest()
            .addPackages(false, new Filter<ArchivePath> () {
               @Override
               public boolean include(ArchivePath path)
               {
                  return !path.get().contains("TestCase");
               }}, "org.jboss.test.ws.jaxws.jbws2278")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2278/WEB-INF/jboss-web.xml"), "jboss-web.xml")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2278/WEB-INF/wsdl/Test.wsdl"), "wsdl/Test.wsdl")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2278/WEB-INF/web.xml"));
      return archive;
   }

   @Before
   public void setup() throws Exception
   {
      if (port11 == null) {
         URL wsdlURL = new URL(baseURL + "/soap11?wsdl");
         QName serviceName = new QName("http://org.jboss.test.ws/jbws2278", "TestService");
   
         Service service = Service.create(wsdlURL, serviceName);
         port11 = service.getPort(new QName("http://org.jboss.test.ws/jbws2278", "TestEndpointSoap11Port"), TestEndpoint.class);
         port12 = service.getPort(new QName("http://org.jboss.test.ws/jbws2278", "TestEndpointSoap12Port"), TestEndpoint.class);
   
         @SuppressWarnings("rawtypes")
         List<Handler> handlerChain11 = new ArrayList<Handler>();
         handlerChain11.add(new TestHandler(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE, SOAPConstants.SOAP_1_1_CONTENT_TYPE));
         ((BindingProvider)port11).getBinding().setHandlerChain(handlerChain11);
   
         @SuppressWarnings("rawtypes")
         List<Handler> handlerChain12 = new ArrayList<Handler>();
         handlerChain12.add(new TestHandler(SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE, SOAPConstants.SOAP_1_2_CONTENT_TYPE));
         ((BindingProvider)port12).getBinding().setHandlerChain(handlerChain12);
      }
   }
   
   @AfterClass
   public static void cleanup()
   {
      port11 = null;
      port12 = null;
   }

   @Test
   @RunAsClient
   public void testCallSoap11() throws Exception
   {
      final String message = "Hello!!";
      String response = port11.echo(message);
      assertEquals(message, response);
   }

   @Test
   @RunAsClient
   public void testCheckedExceptionSoap11() throws Exception
   {
      try
      {
         port11.echo(TestEndpointImpl.TEST_EXCEPTION);
         fail("Expected TestException not thrown.");
      }
      catch (TestException_Exception te)
      {
         //OK
      }
   }

   @Test
   @RunAsClient
   public void testRuntimeExceptionSoap11()
   {
      try
      {
         port11.echo(TestEndpointImpl.RUNTIME_EXCEPTION);
         fail("Expected Exception not thrown.");
      }
      catch (Exception e)
      {
         assertTrue(e.getMessage().startsWith("Simulated failure"));
      }
   }

   @Test
   @RunAsClient
   public void testCallSoap12() throws Exception
   {
      final String message = "Hello!!";
      String response = port12.echo(message);
      assertEquals(message, response);
   }

   @Test
   @RunAsClient
   public void testCheckedExceptionSoap12() throws Exception
   {
      try
      {
         port12.echo(TestEndpointImpl.TEST_EXCEPTION);
         fail("Expected TestException not thrown.");
      }
      catch (TestException_Exception te)
      {
         //OK
      }
   }

   @Test
   @RunAsClient
   public void testRuntimeExceptionSoap12()
   {
      try
      {
         port12.echo(TestEndpointImpl.RUNTIME_EXCEPTION);
         fail("Expected Exception not thrown.");
      }
      catch (Exception e)
      {
         assertTrue(e.getMessage().startsWith("Simulated failure"));
      }
   }
}