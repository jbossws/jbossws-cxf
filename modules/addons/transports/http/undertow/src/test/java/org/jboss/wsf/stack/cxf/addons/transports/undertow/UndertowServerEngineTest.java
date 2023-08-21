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
package org.jboss.wsf.stack.cxf.addons.transports.undertow;

import io.undertow.server.HttpServerExchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.cxf.Bus;
import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.configuration.NullConfigurer;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for UndertowServerEngineTest
 * 
 * @author Magesh Kumar B <mageshbk@jboss.com> (C) 2011 Red Hat Inc.
 * @author alessio.soldano@jboss.com
 * @author <a href="mailto:ema@redhat.com">Jim Ma</a>
 * @since 20-Aug-2010
 *
 */
public class UndertowServerEngineTest {

    private static final int THREAD_COUNT = 50;
    private Bus bus;
    private IMocksControl control;
    private UndertowServerEngineFactory factory;
    private static List<UndertowServerEngine> servers = Collections.synchronizedList(new ArrayList<UndertowServerEngine>());

    
   @Before
   public void setUp() throws Exception
   {
      control = EasyMock.createNiceControl();
      bus = control.createMock(Bus.class);
      Configurer configurer = new NullConfigurer();
      bus.getExtension(Configurer.class);
      EasyMock.expectLastCall().andReturn(configurer).anyTimes();
   }

   @Test
   public void testEngineRetrieval() throws Exception
   {
      control.replay();
      factory = new UndertowServerEngineFactory(bus);
      UndertowServerEngine engine = factory.createHttpServerEngine("localhost", 9234, "http");
      assertTrue(engine == factory.retrieveHttpServerEngine(9234));
      factory.destroyForPort(1234);
      control.verify();
   }

   @Test
   public void testHttpAndHttps() throws Exception
   {
      control.replay();
      factory = new UndertowServerEngineFactory(bus);
      UndertowServerEngine engine = factory.createHttpServerEngine("localhost", 9234, "http");
      assertTrue("http".equals(engine.getProtocol()));
      System.out.println("[JBWS-3702] FIXME: Add support for https protocol");
      //UndertowServerEngine engine2 = factory.createHttpServerEngine("localhost", 9235, "https");
      //assertTrue("https".equals(engine2.getProtocol()));
      factory.destroyForPort(9234);
      //factory.destroyForPort(9235);
      
      control.verify();
   }

   @Test
   public void testMultiThreaded()
   {
      Thread threads[] = new Thread[THREAD_COUNT];
      int i = 0;
      // Initialize the threads
      for (i = 0; i < THREAD_COUNT; i++)
      {
         threads[i] = new Thread(new FactoryInvoker());
      }
      // Start the threads
      for (i = 0; i < THREAD_COUNT; i++)
      {
         threads[i].start();
      }
      // Wait for all threads to complete
      while (servers.size() != THREAD_COUNT) {
         try
         {
            Thread.sleep(100);
         }
         catch (InterruptedException ie)
         {
            // Ignore
         }
      }

      UndertowServerEngine sharedEngine = servers.get(0);
      for (UndertowServerEngine engine : servers)
      {
         assertEquals(sharedEngine, engine);
      }
   }

   @Test
   public void testHandler() throws Exception
   {
      MyTestHandler handler1 = new MyTestHandler();
      MyTestHandler handler2 = new MyTestHandler();
      control.replay();
      factory = new UndertowServerEngineFactory(bus);
      String urlStr1 = "http://localhost:9234/hello/test";
      String urlStr2 = "http://localhost:9234/hello233/test";
      UndertowServerEngine engine = factory.createHttpServerEngine("localhost", 9234, "http");
      engine.addHandler(urlStr1, handler1);
      engine.addHandler(urlStr2, handler2);
      pingServer(new URL(urlStr1));
      pingServer(new URL(urlStr2));
      assertEquals(1, handler1.count.get());
      assertEquals(1, handler2.count.get());
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
         StringBuilder sb = new StringBuilder();
         BufferedReader br = new BufferedReader(new InputStreamReader(connection1.getInputStream(), StandardCharsets.UTF_8));
         String line;
         while ((line = br.readLine()) != null) {
            sb.append(line);
         }
         assertEquals("Hello", sb.toString());
         connection1.disconnect();
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }

   private class MyTestHandler implements io.undertow.server.HttpHandler
   {
      AtomicInteger count = new AtomicInteger(0);

      public MyTestHandler()
      {
         
      }


      @Override
      public void handleRequest(HttpServerExchange exchange) throws Exception
      {
         count.incrementAndGet();
         exchange.setStatusCode(200);
         OutputStream os = exchange.getOutputStream();
         os.write("Hello".getBytes());
         os.flush();
      }
   }

   private class FactoryInvoker implements Runnable
   {
      private UndertowServerEngineFactory _factory;

      FactoryInvoker()
      {
         _factory = new UndertowServerEngineFactory(null);
      }

      public void run()
      {
         UndertowServerEngine engine = null;
         try
         {
            // Delay makes sure the try blocks are initialized before calling createHttpServerEngine,
            // enhances the chance to enter the createHttpServerEngine simultaneously.
            try
            {
               Thread.sleep(10);
            }
            catch (InterruptedException ie)
            {
               // Ignore
            }
            engine = _factory.createHttpServerEngine("127.0.0.1", 18001, "http");
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
         servers.add(engine);
      }
   }
}
