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
package org.jboss.test.ws.jaxws.cxf.jbws3713;

import org.apache.cxf.frontend.ClientProxy;
import org.jboss.wsf.stack.cxf.client.Constants;
import org.jboss.wsf.stack.cxf.client.UseNewBusFeature;
import org.jboss.wsf.stack.cxf.client.UseTCCLBusFeature;
import org.jboss.wsf.stack.cxf.client.UseThreadBusFeature;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebServiceFeature;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * A helper class creating a pool of JAXWS clients that invoke the test endpoint.
 * Here the JAXWS services for the clients are created in a ThreadLocal.
 * Returns the number of Bus instances created to run the clients.
 * 
 * @author alessio.soldano@jboss.com
 * @since 04-Oct-2013
 *
 */
public class HelperUsignThreadLocal
{
   public Integer run(final URL wsdlURL, final int size, final int calls) {
      return run(wsdlURL, null, size, calls);
   }
   
   public Integer run(final URL wsdlURL, final String strategyName, final int size, final int calls) {
      final WebServiceFeature feature;
      final String strategy;
      if (strategyName != null) {
         feature = convertToFeature(strategyName);
         strategy = strategyName;
      } else {
         feature = null;
         if (System.getSecurityManager() == null) {
            strategy = System.getProperty(Constants.JBWS_CXF_JAXWS_CLIENT_BUS_STRATEGY, null);
         } else {

            strategy = AccessController.doPrivileged(new PrivilegedAction<String>() {
               @Override
               public String run() {
                  return System.getProperty(Constants.JBWS_CXF_JAXWS_CLIENT_BUS_STRATEGY, null);
               }
            });

         }
      }
      final BusCounter busCounter = new BusCounter();
      final ThreadLocal<HelloWs> port = createPortThreadLocal(wsdlURL, feature, busCounter);
      final ThreadFactory threadFactory = new ThreadFactory()
      {
         private AtomicInteger i = new AtomicInteger(0);
         
         @Override
         public Thread newThread(Runnable r)
         {
            return new Thread(r, "JBWS3373-TL-thread-" + i.getAndIncrement() + "-" + strategy);
         }
      };
      ExecutorService es = Executors.newFixedThreadPool(size, threadFactory);
      List<Client> clients = new ArrayList<HelperUsignThreadLocal.Client>();
      for (int i = 0; i < calls; i++) {
         clients.add(new Client(port));
      }
      int count = 0;
      try
      {
         List<Future<Boolean>> futures = es.invokeAll(clients);
         for (Future<Boolean> f : futures)
         {
            if (f.get()) {
               count++;
            }
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         es.shutdown();
      }
      if (count != calls) {
         throw new RuntimeException((calls - count) +  " client invocation(s) failed!");
      }
      return busCounter.getCount();
   }
   
   private static WebServiceFeature convertToFeature(final String strategy) {
      if (strategy.equals(Constants.NEW_BUS_STRATEGY)) {
         return new UseNewBusFeature();
      } else if (strategy.equals(Constants.THREAD_BUS_STRATEGY)) {
         return new UseThreadBusFeature();
      } else if (strategy.equals(Constants.TCCL_BUS_STRATEGY)) {
         return new UseTCCLBusFeature();
      } else {
         throw new RuntimeException("Unexpected strategy: " + strategy);
      }
   }
   
   private static ThreadLocal<HelloWs> createPortThreadLocal(final URL wsdlURL, final WebServiceFeature feature, final BusCounter busCounter)
   {
      return new ThreadLocal<HelloWs>()
      {
         @Override
         protected HelloWs initialValue()
         {
            QName qname = new QName("http://hello/test", "HelloService");
            Service service = feature != null ? Service.create(wsdlURL, qname, feature) : Service.create(wsdlURL, qname);
            HelloWs helloPort = service.getPort(HelloWs.class);
            org.apache.cxf.endpoint.Client cxfClient = ClientProxy.getClient(helloPort);
            busCounter.count(cxfClient.getBus());
            return helloPort;
         }
      };
   }

   private static class Client implements Callable<Boolean>
   {
      private final ThreadLocal<HelloWs> port;

      public Client(final ThreadLocal<HelloWs> port)
      {
         this.port = port;
      }

      @Override
      public Boolean call() throws Exception
      {
         HelloRequest request = new HelloRequest();
         request.setInput("hi");
         HelloResponse response = port.get().doHello(request);
         return response.getMultiHello().contains("hi");
      }

   }
}
