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
package org.jboss.test.ws.jaxws.samples.exception;

import java.io.File;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ExceptionServletTestCase extends JBossWSTest
{
   @Deployment(name="jaxws-samples-exception-client", testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-exception-client.war");
      archive
         .addManifest()
         .addClass(org.jboss.test.helper.ClientHelper.class)
         .addClass(org.jboss.test.helper.TestServlet.class)
         .addClass(org.jboss.test.ws.jaxws.samples.exception.ExceptionHelper.class)
         .addClass(org.jboss.test.ws.jaxws.samples.exception.SOAP12ExceptionHelper.class)
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/handlerchain/WEB-INF/permissions.xml"), "permissions.xml")
         .addPackage("org.jboss.test.ws.jaxws.samples.exception.client");
      return archive;
   }

   @Deployment(name = "jaxws-samples-exception-jse", testable = false)
   public static WebArchive createDeployment1() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-exception-jse.war");
      archive
         .addManifest()
         .addClass(org.jboss.test.ws.jaxws.samples.exception.server.EndpointImpl.class)
         .addClass(org.jboss.test.ws.jaxws.samples.exception.server.ExceptionEndpoint.class)
         .addClass(org.jboss.test.ws.jaxws.samples.exception.server.ExceptionEndpointImpl.class)
         .addClass(org.jboss.test.ws.jaxws.samples.exception.server.SOAP12EndpointImpl.class)
         .addClass(org.jboss.test.ws.jaxws.samples.exception.server.SOAP12ExceptionEndpointImpl.class)
         .addClass(org.jboss.test.ws.jaxws.samples.exception.server.ServerHandler.class)
         .addClass(org.jboss.test.ws.jaxws.samples.exception.server.UserException.class)
         .addAsResource("org/jboss/test/ws/jaxws/samples/exception/server/jaxws-handlers-server.xml")
         .addClass(org.jboss.test.ws.jaxws.samples.exception.server.jaxws.ThrowApplicationException.class)
         .addClass(org.jboss.test.ws.jaxws.samples.exception.server.jaxws.ThrowApplicationExceptionResponse.class)
         .addClass(org.jboss.test.ws.jaxws.samples.exception.server.jaxws.ThrowRuntimeException.class)
         .addClass(org.jboss.test.ws.jaxws.samples.exception.server.jaxws.ThrowRuntimeExceptionResponse.class)
         .addClass(org.jboss.test.ws.jaxws.samples.exception.server.jaxws.ThrowSoapFaultException.class)
         .addClass(org.jboss.test.ws.jaxws.samples.exception.server.jaxws.ThrowSoapFaultExceptionResponse.class)
         .addClass(org.jboss.test.ws.jaxws.samples.exception.server.jaxws.UserExceptionBean.class)
         .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/exception/META-INF/permissions.xml"), "permissions.xml")
         .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/exception/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-samples-exception-client")
   public void testRuntimeException() throws Exception
   {
      assertEquals("1", runTestInContainer("testRuntimeException"));
   }

   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-samples-exception-client")
   public void testSoapFaultException() throws Exception
   {
      assertEquals("1", runTestInContainer("testSoapFaultException"));
   }

   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-samples-exception-client")
   public void testApplicationException() throws Exception
   {
      assertEquals("1", runTestInContainer("testApplicationException"));
   }

   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-samples-exception-client")
   public void testRuntimeExceptionSOAP12() throws Exception
   {
      assertEquals("1", runTestInContainerSOAP12("testRuntimeException"));
   }

   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-samples-exception-client")
   public void testSoapFaultExceptionSOAP12() throws Exception
   {
      assertEquals("1", runTestInContainerSOAP12("testSoapFaultException"));
   }

   @Test
   @RunAsClient
   @OperateOnDeployment("jaxws-samples-exception-client")
   public void testApplicationExceptionSOAP12() throws Exception
   {
      assertEquals("1", runTestInContainerSOAP12("testApplicationException"));
   }
   
   private String runTestInContainer(String test) throws Exception
   {
      URL url = new URL("http://" + getServerHost()
            + ":" + getServerPort() + "/jaxws-samples-exception-client?path=/jaxws-samples-exception-jse/ExceptionEndpointService&method=" + test
            + "&helper=" + ExceptionHelper.class.getName());
      return IOUtils.readAndCloseStream(url.openStream());
   }
   
   private String runTestInContainerSOAP12(String test) throws Exception
   {
      URL url = new URL("http://" + getServerHost()
            + ":" + getServerPort() + "/jaxws-samples-exception-client?path=/jaxws-samples-exception-jse/SOAP12ExceptionEndpointService&method=" + test
            + "&helper=" + SOAP12ExceptionHelper.class.getName());
      return IOUtils.readAndCloseStream(url.openStream());
   }
}
