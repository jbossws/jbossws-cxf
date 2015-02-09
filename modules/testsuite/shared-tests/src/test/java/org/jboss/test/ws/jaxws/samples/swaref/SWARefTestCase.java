/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.swaref;

import java.net.URL;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.wsf.test.JBossWSTest;
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

   protected void setUp() throws Exception {
      data = new DataHandler("Client data", "text/plain");
   }

   @Test
   @RunAsClient
   public void testBeanAnnotationWithBare() throws Exception
   {
      setUp();
      Service service = Service.create(new URL(baseURL + "/jaxws-swaref/BareEndpointService/BareEndpoint?wsdl"), bareServiceQName);
      BareEndpoint port = service.getPort(BareEndpoint.class);
      DocumentPayload response = port.beanAnnotation(new DocumentPayload(data));
      assertTrue(response.getData().getContent().equals("Server data"));
   }

   @Test
   @RunAsClient
   public void testBeanAnnotationWithWrapped() throws Exception
   {
      setUp();
      Service service = Service.create(new URL(baseURL+"/jaxws-swaref/WrappedEndpointService/WrappedEndpoint?wsdl"), wrappedServiceQName);
      WrappedEndpoint port = service.getPort(WrappedEndpoint.class);

      DocumentPayload response = port.beanAnnotation(new DocumentPayload(data), "Wrapped test");
      assertTrue(response.getData().getContent().equals("Server data"));
   }

   @Test
   @RunAsClient
   public void testParameterAnnotationWithWrapped() throws Exception
   {
      setUp();
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
      setUp();
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
      setUp();
      //[JBWS-2708]
      Service service = Service.create(new URL(baseURL+"/jaxws-swaref/WrappedEndpointService/WrappedEndpoint?wsdl"), wrappedServiceQName);
      WrappedEndpoint port = service.getPort(WrappedEndpoint.class);
      DocumentPayloadWithList payload = new DocumentPayloadWithList();
      payload.getData().add(data);

      DocumentPayloadWithList response = port.listAnnotation(payload, "Wrapped test");
      assertTrue(response.getData().get(0).getContent().equals("Server data"));
   }
}
