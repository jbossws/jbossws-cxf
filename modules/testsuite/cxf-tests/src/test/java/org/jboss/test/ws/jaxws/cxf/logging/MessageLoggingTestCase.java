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
package org.jboss.test.ws.jaxws.cxf.logging;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.wsf.stack.cxf.client.UseThreadBusFeature;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests configuration of message exchange logging using API
 *
 * @author alessio.soldano@jboss.com
 * @since 08-Jul-2010
 */
@RunWith(Arquillian.class)
public class MessageLoggingTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static JavaArchive createDeployment() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-cxf-logging.jar");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.jboss.ws.cxf.jbossws-cxf-client\n"))
            .addClass(org.jboss.test.ws.jaxws.cxf.logging.CustomInInterceptor.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.logging.LoggingFeatureEndpointImpl.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.logging.LoggingInterceptorsEndpointImpl.class)
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/logging/META-INF/permissions.xml"), "permissions.xml");
      return archive;
   }

   @Test
   @RunAsClient
   public void testLoggingFeature() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/jaxws-cxf-logging/LoggingFeatureService/LoggingFeatureEndpoint?wsdl");
      QName serviceName = new QName("http://logging.cxf.jaxws.ws.test.jboss.org/", "LoggingFeatureService");
      Service service = Service.create(wsdlURL, serviceName);
      QName portQName = new QName("http://logging.cxf.jaxws.ws.test.jboss.org/", "LoggingFeatureEndpointPort");
      LoggingEndpoint port = (LoggingEndpoint)service.getPort(portQName, LoggingEndpoint.class);

      //This is actually just a sample, the test does not actually assert the logs are written on server side for the exchanges message
      //The CXF @Feature on the endpoint ensures exchanged messages are written to the server log
      assertEquals("foo", port.echo("foo"));
   }

   @Test
   @RunAsClient
   public void testLoggingWithCustomInterceptors() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/jaxws-cxf-logging/LoggingInterceptorsService/LoggingInterceptorsEndpoint?wsdl");
      QName serviceName = new QName("http://logging.cxf.jaxws.ws.test.jboss.org/", "LoggingInterceptorsService");
      Service service = Service.create(wsdlURL, serviceName);
      QName portQName = new QName("http://logging.cxf.jaxws.ws.test.jboss.org/", "LoggingInterceptorsEndpointPort");
      LoggingEndpoint port = (LoggingEndpoint)service.getPort(portQName, LoggingEndpoint.class);
      assertEquals("foo", port.echo("foo"));
   }

   @Test
   @RunAsClient
   public void testClientLogging() throws Exception
   {
      URL wsdlURL = new URL(baseURL + "/jaxws-cxf-logging/LoggingFeatureService/LoggingFeatureEndpoint?wsdl");
      QName serviceName = new QName("http://logging.cxf.jaxws.ws.test.jboss.org/", "LoggingFeatureService");

      Bus bus = BusFactory.newInstance().createBus();
      try
      {
         //install the a LoggingInInterceptor in the bus used for the client
         OutputStream out = new ByteArrayOutputStream();
         LoggingInInterceptor myLoggingInterceptor = new LoggingInInterceptor(new PrintWriter(out, true));
         bus.getInInterceptors().add(myLoggingInterceptor);
         BusFactory.setThreadDefaultBus(bus);

         Service service = Service.create(wsdlURL, serviceName, new UseThreadBusFeature());
         QName portQName = new QName("http://logging.cxf.jaxws.ws.test.jboss.org/", "LoggingFeatureEndpointPort");
         LoggingEndpoint port = (LoggingEndpoint)service.getPort(portQName, LoggingEndpoint.class);
         String content = "foo";
         port.echo(content);
         String s = out.toString();
         assertTrue("'" + content + "' not found in captured message: \n" + s, s.contains(content));
      }
      finally
      {
         bus.shutdown(true);
      }
   }
}
