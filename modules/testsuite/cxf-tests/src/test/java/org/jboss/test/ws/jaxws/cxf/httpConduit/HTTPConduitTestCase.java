/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.httpConduit;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transport.http.HTTPConduitFactory;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.stack.cxf.client.Constants;
import org.jboss.wsf.stack.cxf.client.configuration.AbstractHTTPConduitFactoryWrapper;
import org.jboss.wsf.stack.cxf.client.configuration.DefaultHTTPConduitFactoryWrapper;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-3901] Testcase for HTTPConduit default value setup
 *
 * @author alessio.soldano@jboss.com
 * @since 02-Apr-2015
 */
@RunWith(Arquillian.class)
public class HTTPConduitTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-cxf-httpConduit.war");
      archive.addClass(org.jboss.test.ws.jaxws.cxf.httpConduit.EndpointOneImpl.class);
      return archive;
   }

   @Test
   @RunAsClient
   public void testDefaultWrapperInstalled() throws Exception {
      Bus bus = BusFactory.newInstance().createBus();
      try {
         HTTPConduitFactory hcf = bus.getExtension(HTTPConduitFactory.class);
         assertNotNull(hcf);
         assertTrue(hcf instanceof DefaultHTTPConduitFactoryWrapper);
         
         URL wsdlURL = new URL(baseURL + "/ServiceOne" + "?wsdl");
         Service service = Service.create(wsdlURL, new QName("http://org.jboss.ws.jaxws.cxf/httpConduit", "ServiceOne"));
         EndpointOne port = service.getPort(new QName("http://org.jboss.ws.jaxws.cxf/httpConduit", "EndpointOnePort"), EndpointOne.class);
         assertEquals("Foo", port.echo("Foo"));
      } finally {
         bus.shutdown(true);
      }
   }
   
   @Test
   @RunAsClient
   public void testWrapperWithMap() throws Exception {
      Bus bus = BusFactory.newInstance().createBus();
      BusFactory.setThreadDefaultBus(bus);
      try {
         Map<String, Object> map = new HashMap<String, Object>();
         map.put(Constants.CXF_CLIENT_ALLOW_CHUNKING, true);
         map.put(Constants.CXF_CLIENT_CHUNKING_THRESHOLD, 8192);
         map.put(Constants.CXF_CLIENT_CONNECTION_TIMEOUT, 16384L);
         map.put(Constants.CXF_CLIENT_RECEIVE_TIMEOUT, 163840L);
         map.put(Constants.CXF_TLS_CLIENT_DISABLE_CN_CHECK, true);
         replaceWrapper(map, bus);
         
         URL wsdlURL = new URL(baseURL + "/ServiceOne" + "?wsdl");
         Service service = Service.create(wsdlURL, new QName("http://org.jboss.ws.jaxws.cxf/httpConduit", "ServiceOne"));
         EndpointOne port = service.getPort(new QName("http://org.jboss.ws.jaxws.cxf/httpConduit", "EndpointOnePort"), EndpointOne.class);
         
         HTTPConduit conduit = (HTTPConduit)ClientProxy.getClient(port).getConduit();
         HTTPClientPolicy client = conduit.getClient();
         assertNotNull(client);
         assertEquals(true, client.isAllowChunking());
         assertEquals(8192, client.getChunkingThreshold());
         assertEquals(16384, client.getConnectionTimeout());
         assertEquals(163840, client.getReceiveTimeout());
         assertEquals(true, conduit.getTlsClientParameters().isDisableCNCheck());
         
         assertEquals("Foo", port.echo("Foo"));
      } finally {
         bus.shutdown(true);
      }
   }
   
   @Test
   @RunAsClient
   public void testTimeout() throws Exception {
      Bus bus = BusFactory.newInstance().createBus();
      BusFactory.setThreadDefaultBus(bus);
      try {
         Map<String, Object> map = new HashMap<String, Object>();
         map.put(Constants.CXF_CLIENT_CONNECTION_TIMEOUT, 6000L);
         map.put(Constants.CXF_CLIENT_RECEIVE_TIMEOUT, 1000L);
         replaceWrapper(map, bus);
         
         URL wsdlURL = new URL(baseURL + "/ServiceOne" + "?wsdl");
         Service service = Service.create(wsdlURL, new QName("http://org.jboss.ws.jaxws.cxf/httpConduit", "ServiceOne"));
         EndpointOne port = service.getPort(new QName("http://org.jboss.ws.jaxws.cxf/httpConduit", "EndpointOnePort"), EndpointOne.class);
         
         try {
            port.echo("wait");
            fail("Timeout exeception is expected");
         } catch (Exception e) {
            //expected
         }
         
         assertEquals("Foo", port.echo("Foo"));
      } finally {
         bus.shutdown(true);
      }
   }
   
   
   
   private DefaultHTTPConduitFactoryWrapper replaceWrapper(Map<String, Object> args, Bus bus)
   {
      HTTPConduitFactory hcf = bus.getExtension(HTTPConduitFactory.class);
      //replace wrapper
      DefaultHTTPConduitFactoryWrapper w = new DefaultHTTPConduitFactoryWrapper(args, false,
            ((AbstractHTTPConduitFactoryWrapper) hcf).getDelegate());
      bus.setExtension(w, HTTPConduitFactory.class);
      return w;
   }
}
