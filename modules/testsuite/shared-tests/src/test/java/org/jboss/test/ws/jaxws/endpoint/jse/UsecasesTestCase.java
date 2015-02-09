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
package org.jboss.test.ws.jaxws.endpoint.jse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.soap.MTOMFeature;
import javax.xml.ws.soap.SOAPBinding;

import org.jboss.test.ws.jaxws.endpoint.jse.endpoints.DHRequest;
import org.jboss.test.ws.jaxws.endpoint.jse.endpoints.DHResponse;
import org.jboss.test.ws.jaxws.endpoint.jse.endpoints.Endpoint1Iface;
import org.jboss.test.ws.jaxws.endpoint.jse.endpoints.Endpoint1Impl;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;

/**
 * Tests endpoint dynamic publishing in JSE environment.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
@RunWith(Arquillian.class)
public final class UsecasesTestCase extends JBossWSTest
{
   private static WebServiceFeature[] mtomEnabled = new WebServiceFeature[] { new MTOMFeature(true) };
   
   private static int port1 = 8871;
   private static int port2 = 8872;

   @Test
   @RunAsClient
   public void testDifferentPortsSameContext() throws Exception
   {
      String publishURL1 = "http://" + getServerHost() + ":" + port1 + "/jaxws-endpoint/";
      Endpoint endpoint1 = publishEndpoint1(new Endpoint1Impl(), publishURL1);

      String publishURL2 = "http://" + getServerHost() + ":" + port2 + "/jaxws-endpoint";
      Endpoint endpoint2 = publishEndpoint2(new Endpoint1Impl(), publishURL2);

      invokeEndpoint1(publishURL1);
      invokeEndpoint1(publishURL2);

      endpoint1.stop();
      endpoint2.stop();
   }

   @Test
   @RunAsClient
   public void testDifferentPortsNoContext() throws Exception
   {
      String publishURL1 = "http://" + getServerHost() + ":" + port1 + "/";
      Endpoint endpoint1 = publishEndpoint1(new Endpoint1Impl(), publishURL1);

      String publishURL2 = "http://" + getServerHost() + ":" + port2;
      Endpoint endpoint2 = publishEndpoint2(new Endpoint1Impl(), publishURL2);

      invokeEndpoint1(publishURL1);
      if (isIntegrationCXF())
      {
         //sun.net.www.protocol.http.HttpURLConnection barfs on addresses like http://localhost:8872?wsdl
         invokeEndpoint1(publishURL2.replace(String.valueOf(port2), port2 + "/"));
      }
      else
      {
         invokeEndpoint1(publishURL2);
      }

      endpoint1.stop();
      endpoint2.stop();
   }

   @Test
   @RunAsClient
   public void testDifferentPortsAndLongPaths() throws Exception
   {
      String publishURL1 = "http://" + getServerHost() + ":" + port1 + "/jaxws-endpoint/endpoint/long/path/";
      Endpoint endpoint1 = publishEndpoint3(new Endpoint1Impl(), publishURL1);

      String publishURL2 = "http://" + getServerHost() + ":" + port2 + "/jaxws-endpoint/endpoint/long/path";
      Endpoint endpoint2 = publishEndpoint1(new Endpoint1Impl(), publishURL2);

      invokeEndpoint1(publishURL1);
      invokeEndpoint1(publishURL2);

      endpoint1.stop();
      endpoint2.stop();
   }

   @Test
   @RunAsClient
   public void testSamePortsAndAlmostIdenticalLongPaths() throws Exception
   {
      String publishURL1 = "http://" + getServerHost() + ":" + port1 + "/jaxws-endpoint/endpoint/number1/";
      Endpoint endpoint1 = publishEndpoint2(new Endpoint1Impl(), publishURL1);

      String publishURL2 = "http://" + getServerHost() + ":" + port1 + "/jaxws-endpoint/endpoint/number11";
      Endpoint endpoint2 = publishEndpoint3(new Endpoint1Impl(), publishURL2);

      invokeEndpoint2(publishURL1);
      invokeEndpoint2(publishURL2);

      endpoint1.stop();
      endpoint2.stop();
   }

   @Test
   @RunAsClient
   public void testDifferentPortsAndIdenticalPaths() throws Exception
   {
      String publishURL1 = "http://" + getServerHost() + ":" + port1 + "/jaxws-endpoint/endpoint/number1/";
      Endpoint endpoint1 = publishEndpoint1(new Endpoint1Impl(), publishURL1);

      String publishURL2 = "http://" + getServerHost() + ":" + port2 + "/jaxws-endpoint/endpoint/number1";
      Endpoint endpoint2 = publishEndpoint2(new Endpoint1Impl(), publishURL2);

      invokeEndpoint2(publishURL1);
      invokeEndpoint2(publishURL2);

      endpoint1.stop();
      endpoint2.stop();
   }

   @Test
   @RunAsClient
   public void testEndpointThrowingException() throws Exception
   {
      String publishURL = "http://" + getServerHost() + ":" + port1 + "/jaxws-endpoint/endpoint/number1";
      Endpoint endpoint = publishEndpoint3(new Endpoint1Impl(), publishURL);
      invokeEndpoint3(publishURL);
      endpoint.stop();
   }

   @Test
   @RunAsClient
   public void testEndpointProcessingAttachments() throws Exception
   {
      for (int i = 0; i < 2; i++)
      {
         String publishURL = "http://" + getServerHost() + ":" + port1 + "/jaxws-endpoint/endpoint/number1";
         Endpoint endpoint = publishEndpoint3(new Endpoint1Impl(), publishURL);
         invokeEndpoint4(publishURL);
         endpoint.stop();
      }
   }

   private Endpoint publishEndpoint1(Object epImpl, String publishURL)
   {
      Endpoint endpoint = Endpoint.create(SOAPBinding.SOAP11HTTP_BINDING, epImpl);
      endpoint.publish(publishURL);
      return endpoint;
   }
   
   private Endpoint publishEndpoint2(Object epImpl, String publishURL)
   {
      Endpoint endpoint = Endpoint.create(epImpl);
      endpoint.publish(publishURL);
      return endpoint;
   }

   private Endpoint publishEndpoint3(Object epImpl, String publishURL)
   {
      return Endpoint.publish(publishURL, epImpl);
   }

   private void invokeEndpoint1(String publishURL) throws Exception
   {
      Endpoint1Iface port = this.getProxy(publishURL);

      String helloWorld = "Hello world!";
      Object retObj = port.echo(helloWorld);
      assertEquals(helloWorld, retObj);
   }

   private void invokeEndpoint2(String publishURL) throws Exception
   {
      Endpoint1Iface port = this.getProxy(publishURL);

      String helloWorld = "Hello world!";
      
      assertEquals(0, port.getCount());
      Object retObj = port.echo(helloWorld);

      assertEquals(helloWorld, retObj);
      assertEquals(1, port.getCount());
      
      port.echo(helloWorld);
      assertEquals(2, port.getCount());
   }

   private void invokeEndpoint3(String publishURL) throws Exception
   {
      Endpoint1Iface port = this.getProxy(publishURL);

      try
      {
         port.getException();
         fail("Failure expected");
      }
      catch (Exception e)
      {
         assertEquals("Ooops", e.getMessage());
      }
   }
   
   private void invokeEndpoint4(String publishURL) throws Exception
   {
      Endpoint1Iface port = this.getProxy(publishURL, mtomEnabled);

      DataSource ds = new DataSource()
      {
         public String getContentType() { return "text/plain"; }
         public InputStream getInputStream() throws IOException { return new ByteArrayInputStream("some string".getBytes()); }
         public String getName() { return "none"; }
         public OutputStream getOutputStream() throws IOException { return null; }
      };
      DataHandler dh = new DataHandler(ds);
      DHResponse response = port.echoDataHandler(new DHRequest(dh));
      assertNotNull(response);

      Object content = response.getDataHandler().getContent();
      assertEquals("Server data", content);
      String contentType = response.getDataHandler().getContentType();
      assertEquals("text/plain", contentType);
   }

   private Endpoint1Iface getProxy(String publishURL) throws Exception
   {
      return this.getProxy(publishURL, null);
   }

   private Endpoint1Iface getProxy(String publishURL, WebServiceFeature[] features) throws Exception
   {
      URL wsdlURL = new URL(publishURL + "?wsdl");
      QName qname = new QName("http://org.jboss.ws/jaxws/endpoint/jse/endpoints/", "Endpoint1Impl");
      Service service = Service.create(wsdlURL, qname);
      return (Endpoint1Iface)service.getPort(Endpoint1Iface.class, features);
   }

}
