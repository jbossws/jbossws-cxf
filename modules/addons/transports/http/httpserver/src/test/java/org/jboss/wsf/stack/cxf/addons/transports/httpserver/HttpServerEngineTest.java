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
package org.jboss.wsf.stack.cxf.addons.transports.httpserver;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.cxf.Bus;
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.configuration.spring.ConfigurerImpl;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

/**
 * Tests for HttpServerEngine
 * 
 * @author alessio.soldano@jboss.com
 * @since 20-Aug-2010
 *
 */
public class HttpServerEngineTest extends TestCase {

    private Bus bus;
    private IMocksControl control;
    private HttpServerEngineFactory factory;

    
   public void setUp() throws Exception
   {
      control = EasyMock.createNiceControl();
      bus = control.createMock(Bus.class);
      Configurer configurer = new ConfigurerImpl();
      bus.getExtension(Configurer.class);
      EasyMock.expectLastCall().andReturn(configurer).anyTimes();
   }

   public void testEngineRetrieval() throws Exception
   {
      control.replay();
      factory = new HttpServerEngineFactory(bus);
      HttpServerEngine engine = factory.createHttpServerEngine("localhost", 9234, "http");
      assertTrue(engine == factory.retrieveHttpServerEngine(9234));
      factory.destroyForPort(1234);
      control.verify();
   }

   public void testHttpAndHttps() throws Exception
   {
      control.replay();
      factory = new HttpServerEngineFactory(bus);
      HttpServerEngine engine = factory.createHttpServerEngine("localhost", 9234, "http");
      assertTrue("http".equals(engine.getProtocol()));
      System.out.println("[JBWS-3079] FIXME: Add support for https protocol");
//      HttpServerEngine engine2 = factory.createHttpServerEngine("localhost", 9235, "https");
//      assertTrue("https".equals(engine2.getProtocol()));
      factory.destroyForPort(9234);
//      factory.destroyForPort(9235);
      
      control.verify();
   }

   public void testHandler() throws Exception
   {
      MyTestHandler handler1 = new MyTestHandler();
      MyTestHandler handler2 = new MyTestHandler();
      control.replay();
      factory = new HttpServerEngineFactory(bus);
      String urlStr1 = "http://localhost:9234/hello/test";
      String urlStr2 = "http://localhost:9234/hello233/test";
      HttpServerEngine engine = factory.createHttpServerEngine("localhost", 9234, "http");
      engine.addHandler(urlStr1, handler1);
      engine.addHandler(urlStr2, handler2);
      pingServer(new URL(urlStr1));
      pingServer(new URL(urlStr2));
      assertEquals(1, handler1.count);
      assertEquals(1, handler2.count);
      engine.removeHandler(urlStr1);
      engine.removeHandler(urlStr2);
      engine.shutdown();
      factory.destroyForPort(9234);
      
      control.verify();
   }
   
   private void pingServer(URL url)
   {
      try
      {
         HttpURLConnection connection1 = (HttpURLConnection) url.openConnection();
         connection1.getInputStream();
         connection1.disconnect();
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }

   private class MyTestHandler implements org.jboss.com.sun.net.httpserver.HttpHandler
   {
      volatile int count;

      public MyTestHandler()
      {
         
      }
      
      @Override
      public void handle(org.jboss.com.sun.net.httpserver.HttpExchange exchange) throws IOException {
         count++;
         exchange.sendResponseHeaders(200, 0);
         OutputStream os = exchange.getResponseBody();
         os.write("Hello".getBytes());
         os.flush();
         exchange.close();
      }
   }

}
