/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.gzip;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.helpers.HttpHeaderHelper;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transport.http.gzip.GZIPFeature;
import org.apache.cxf.transport.http.gzip.GZIPInInterceptor;
import org.apache.cxf.transport.http.gzip.GZIPOutInterceptor;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;

/**
 * 
 * @author alessio.soldano@jboss.com
 * @since 15-Sep-2010
 *
 */
public class GZIPTestCase extends JBossWSTest
{
   private String gzipFeatureEndpointURL = "http://" + getServerHost() + ":8080/jaxws-cxf-gzip/HelloWorldService/HelloWorldImpl";

   private HelloWorld port;

   public static Test suite()
   {
      return new JBossWSCXFTestSetup(GZIPTestCase.class, "jaxws-cxf-gzip.jar");
   }
   
   public void testGZIPUsingFeatureOnBus() throws Exception
   {
      Bus bus = BusFactory.newInstance().createBus();
      try
      {
         BusFactory.setThreadDefaultBus(bus);
         
         GZIPFeature gzipFeature = new GZIPFeature();
         gzipFeature.setThreshold(0);
         gzipFeature.initialize(bus);

         HelloWorld port = getPort();
         assertEquals("foo", port.echo("foo"));
      }
      finally
      {
         bus.shutdown(true);
      }
   }
   
   public void testGZIPUsingFeatureOnClient() throws Exception
   {
      HelloWorld port = getPort();
      Client client = ClientProxy.getClient(port);
      GZIPFeature gzipFeature = new GZIPFeature();
      gzipFeature.setThreshold(0);
      gzipFeature.initialize(client, BusFactory.getThreadDefaultBus());
      assertEquals("foo", port.echo("foo"));
   }
   
   public void testGZIPServerSideOnlyInterceptorOnClient() throws Exception
   {
      HelloWorld port = getPort();
      Client client = ClientProxy.getClient(port);
      HTTPConduit conduit = (HTTPConduit)client.getConduit();
      HTTPClientPolicy policy = conduit.getClient();
      //enable Accept gzip, otherwise the server will not try to reply using gzip
      policy.setAcceptEncoding("gzip;q=1.0, identity; q=0.5, *;q=0");
      //add interceptor for decoding gzip message
      client.getInInterceptors().add(new GZIPEnforcingInInterceptor());
      assertEquals("foo", port.echo("foo"));
   }
   
   public void testFailureGZIPServerSideOnlyInterceptorOnClient() throws Exception
   {
      HelloWorld port = getPort();
      Client client = ClientProxy.getClient(port);
      HTTPConduit conduit = (HTTPConduit)client.getConduit();
      HTTPClientPolicy policy = conduit.getClient();
      //enable Accept gzip, otherwise the server will not try to reply using gzip
      policy.setAcceptEncoding("gzip;q=1.0, identity; q=0.5, *;q=0");
      try
      {
         port.echo("foo");
         fail();
      }
      catch (Exception e)
      {
         //expected exception, as the client is not able to decode gzip message
      }
   }
   
   public void testGZIPServerSideOnlyInterceptorsOnBus() throws Exception
   {
      Bus bus = BusFactory.newInstance().createBus();
      try
      {
         //enable Accept gzip, otherwise the server will not try to reply using gzip
         GZIPOutInterceptor outInterceptor = new GZIPOutInterceptor();
         outInterceptor.setThreshold(1024*1024); // 1MB -> no gzip on request
         bus.getOutInterceptors().add(outInterceptor);
         //add interceptor for decoding gzip message
         bus.getInInterceptors().add(new GZIPEnforcingInInterceptor());
         BusFactory.setThreadDefaultBus(bus);
         HelloWorld port = getPort();
         assertEquals("foo", port.echo("foo"));
      }
      finally
      {
         bus.shutdown(true);
      }
   }

   public void testFailureGZIPServerSideOnlyInterceptorsOnBus() throws Exception
   {
      Bus bus = BusFactory.newInstance().createBus();
      try
      {
         //enable Accept gzip, otherwise the server will not try to reply using gzip
         GZIPOutInterceptor outInterceptor = new GZIPOutInterceptor();
         outInterceptor.setThreshold(1024*1024); // 1MB -> no gzip on request
         bus.getOutInterceptors().add(outInterceptor);
         BusFactory.setThreadDefaultBus(bus);
         HelloWorld port = getPort();
         try
         {
            port.echo("foo");
            fail();
         }
         catch (Exception e)
         {
            //expected exception, as the client is not able to decode gzip message
         }
      }
      finally
      {
         bus.shutdown(true);
      }
   }
   
   private HelloWorld getPort() throws MalformedURLException
   {
      if (port == null)
      {
         URL wsdlURL = new URL(gzipFeatureEndpointURL + "?wsdl");
         QName serviceName = new QName("http://org.jboss.ws/jaxws/cxf/gzip", "HelloWorldService");
         Service service = Service.create(wsdlURL, serviceName);
         QName portQName = new QName("http://org.jboss.ws/jaxws/cxf/gzip", "HelloWorldImplPort");
         port = (HelloWorld) service.getPort(portQName, HelloWorld.class);
      }
      return port;
   }
   
   /**
    * A test extension of the GZIPInInterceptor that requires all received messages to be gzip encoded
    *
    */
   private class GZIPEnforcingInInterceptor extends GZIPInInterceptor
   {
      @Override
      public void handleMessage(Message message) throws Fault
      {
         Map<String, List<String>> protocolHeaders = CastUtils.cast((Map<?, ?>) message.get(Message.PROTOCOL_HEADERS));
         if (protocolHeaders != null)
         {
            List<String> contentEncoding = HttpHeaderHelper.getHeader(protocolHeaders,
                  HttpHeaderHelper.CONTENT_ENCODING);
            if (contentEncoding != null && (contentEncoding.contains("gzip") || contentEncoding.contains("x-gzip")))
            {
               super.handleMessage(message);
               return;
            }
         }
         throw new RuntimeException("Content-Encoding gzip not found!");
      }
   }
}
