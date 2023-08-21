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
package org.jboss.test.ws.jaxws.samples.swaref;

import java.net.URL;

import jakarta.activation.DataHandler;
import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test SwARef with different binding styles and @XmlAttachmentRef locations.
 *
 * @author Heiko.Braun@jboss.com
 */
@RunWith(Arquillian.class)
public class SWARefTestCase extends JBossWSTest
{
   private QName bareServiceQName = new QName("http://swaref.samples.jaxws.ws.test.jboss.org/", "BareEndpointService");
   private QName wrappedServiceQName = new QName("http://swaref.samples.jaxws.ws.test.jboss.org/", "WrappedEndpointService");
   private QName rpcLitServiceQName = new QName("http://swaref.samples.jaxws.ws.test.jboss.org/", "RpcLitEndpointService");

   private static DataHandler data;

   @ArquillianResource
   private URL baseURL;

   @Deployment(testable = false)
   public static JavaArchive createDeployments() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-samples-swaref.jar");
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.samples.swaref.BareEndpoint.class)
               .addClass(org.jboss.test.ws.jaxws.samples.swaref.BareEndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.samples.swaref.DocumentPayload.class)
               .addClass(org.jboss.test.ws.jaxws.samples.swaref.DocumentPayloadWithList.class)
               .addClass(org.jboss.test.ws.jaxws.samples.swaref.DocumentPayloadWithoutRef.class)
               .addClass(org.jboss.test.ws.jaxws.samples.swaref.RpcLitEndpoint.class)
               .addClass(org.jboss.test.ws.jaxws.samples.swaref.RpcLitEndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.samples.swaref.WrappedEndpoint.class)
               .addClass(org.jboss.test.ws.jaxws.samples.swaref.WrappedEndpointImpl.class)
               .addClass(org.jboss.test.ws.jaxws.samples.swaref.jaxws.BeanAnnotation.class)
               .addClass(org.jboss.test.ws.jaxws.samples.swaref.jaxws.BeanAnnotationResponse.class)
               .addClass(org.jboss.test.ws.jaxws.samples.swaref.jaxws.ParameterAnnotation.class)
               .addClass(org.jboss.test.ws.jaxws.samples.swaref.jaxws.ParameterAnnotationResponse.class);
      return archive;
   }

   @BeforeClass
   public static void setup() throws Exception {
      data = new DataHandler("Client data", "text/plain");
   }
   
   @AfterClass
   public static void clean() {
      data = null;
   }

   @Test
   @RunAsClient
   public void testBeanAnnotationWithBare() throws Exception
   {
      Service service = Service.create(new URL(baseURL + "/jaxws-swaref/BareEndpointService/BareEndpoint?wsdl"), bareServiceQName);
      BareEndpoint port = service.getPort(BareEndpoint.class);
      DocumentPayload response = port.beanAnnotation(new DocumentPayload(data));
      assertTrue(response.getData().getContent().equals("Server data"));
   }

   @Test
   @RunAsClient
   public void testBeanAnnotationWithWrapped() throws Exception
   {
      Service service = Service.create(new URL(baseURL+"/jaxws-swaref/WrappedEndpointService/WrappedEndpoint?wsdl"), wrappedServiceQName);
      WrappedEndpoint port = service.getPort(WrappedEndpoint.class);

      DocumentPayload response = port.beanAnnotation(new DocumentPayload(data), "Wrapped test");
      assertTrue(response.getData().getContent().equals("Server data"));
   }

   @Test
   @RunAsClient
   public void testParameterAnnotationWithWrapped() throws Exception
   {
      Service service = Service.create(new URL(baseURL+"/jaxws-swaref/WrappedEndpointService/WrappedEndpoint?wsdl"), wrappedServiceQName);
      WrappedEndpoint port = service.getPort(WrappedEndpoint.class);

      DataHandler response = port.parameterAnnotation(new DocumentPayload(data), "Wrapped test", data);
      assertNotNull("Response as null", response);
      assertTrue("Contents are not equal", response.getContent().equals("Server data"));
   }

   @Test
   @RunAsClient
   public void testBeanAnnotationWithRPC() throws Exception
   {
      Service service = Service.create(new URL(baseURL+"/jaxws-swaref/RpcLitEndpointService/RpcLitEndpoint?wsdl"), rpcLitServiceQName);
      RpcLitEndpoint port = service.getPort(RpcLitEndpoint.class);

      DocumentPayload response = port.beanAnnotation( new DocumentPayload(data));
      assertNotNull("Response was null", response);
      assertTrue(response.getData().getContent().equals("Server data"));
   }

   @Test
   @RunAsClient
   public void testListAnnotationWithWrapped() throws Exception
   {
      //[JBWS-2708]
      Service service = Service.create(new URL(baseURL+"/jaxws-swaref/WrappedEndpointService/WrappedEndpoint?wsdl"), wrappedServiceQName);
      WrappedEndpoint port = service.getPort(WrappedEndpoint.class);
      DocumentPayloadWithList payload = new DocumentPayloadWithList();
      payload.getData().add(data);

      DocumentPayloadWithList response = port.listAnnotation(payload, "Wrapped test");
      assertTrue(response.getData().get(0).getContent().equals("Server data"));
   }
}
