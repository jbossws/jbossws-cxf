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
package org.jboss.test.ws.jaxws.samples.webservice;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test the JSR-181 annotation: jakarta.jws.WebService
 *
 * @author Thomas.Diesler@jboss.org
 * @author <a href="jason.greene@jboss.com">Jason T. Greene</a>
 * @since 29-Apr-2005
 */
@RunWith(Arquillian.class)
public class WebServiceJSETestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;

   private static final String WEBSERVICE_03 = "jaxws-samples-webservice03-jse";

   @Deployment(name = WEBSERVICE_03, testable = false)
   public static WebArchive createClientDeployment3() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-webservice03-jse.war");
      archive
         .addManifest()
         .addClass(org.jboss.test.ws.jaxws.samples.webservice.EndpointInterface03.class)
         .addClass(org.jboss.test.ws.jaxws.samples.webservice.JSEBean03.class)
         .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/webservice/WEB-INF03/web.xml"));
      return archive;
   }

   private static final String WEBSERVICE_02 = "jaxws-samples-webservice02-jse";

   @Deployment(name = WEBSERVICE_02, testable = false)
   public static WebArchive createClientDeployment2() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-webservice02-jse.war");
      archive
         .addManifest()
         .addClass(org.jboss.test.ws.jaxws.samples.webservice.JSEBean02.class)
         .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/webservice/WEB-INF02/wsdl/TestService.wsdl"), "wsdl/TestService.wsdl")
         .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/webservice/WEB-INF02/web.xml"));
      return archive;
   }

   private static final String WEBSERVICE_01 = "jaxws-samples-webservice01-jse";

   @Deployment(name = WEBSERVICE_01, testable = false)
   public static WebArchive createClientDeployment1() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-webservice01-jse.war");
      archive
         .addManifest()
         .addClass(org.jboss.test.ws.jaxws.samples.webservice.JSEBean01.class)
         .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/webservice/WEB-INF01/web.xml"));
      return archive;
   }
   
   private EndpointInterface getPort(String endpointURI) throws MalformedURLException
   {
      QName serviceName = new QName("http://www.openuri.org/2004/04/HelloWorld", "EndpointService");
      URL wsdlURL = new URL("http://" + baseURL.getHost() + ":" + baseURL.getPort() + "/" + endpointURI + "?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      return service.getPort(EndpointInterface.class);
   }

   private EndpointInterface03 getPort03(String endpointURI) throws MalformedURLException
   {
      QName serviceName = new QName("http://www.openuri.org/2004/04/HelloWorld", "EndpointService");
      URL wsdlURL = new URL("http://" + getServerHost() + ":" + baseURL.getPort() + "/" + endpointURI + "?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      return service.getPort(EndpointInterface03.class);
   }

   public void webServiceTest(String endpointURI) throws Exception
   {
      String helloWorld = "Hello world!";
      Object retObj = getPort(endpointURI).echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }

   public void webServiceWsdlLocationTest(String endpointURI) throws Exception
   {
      String helloWorld = "Hello world!";
      Object retObj = getPort(endpointURI).echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }

   public void webServiceEndpointInterfaceTest(String endpointURI) throws Exception
   {
      String helloWorld = "Hello Interface!";
      Object retObj = getPort03(endpointURI).echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }

   @Test
   @RunAsClient
   @OperateOnDeployment(WEBSERVICE_01)
   public void testWebServiceTest() throws Exception
   {
      webServiceTest("jaxws-samples-webservice01-jse");
   }

   @Test
   @RunAsClient
   @OperateOnDeployment(WEBSERVICE_02)
   public void testWebServiceWsdlLocation() throws Exception
   {
      webServiceWsdlLocationTest("jaxws-samples-webservice02-jse");
   }

   @Test
   @RunAsClient
   @OperateOnDeployment(WEBSERVICE_03)
   public void testWebServiceEndpointInterface() throws Exception
   {
      webServiceEndpointInterfaceTest("jaxws-samples-webservice03-jse");
   }

}
