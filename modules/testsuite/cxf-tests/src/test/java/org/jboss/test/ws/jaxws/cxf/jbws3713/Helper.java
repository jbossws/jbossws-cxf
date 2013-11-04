/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.jbws3713;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceFeature;

import org.apache.cxf.frontend.ClientProxy;
import org.jboss.wsf.stack.cxf.client.Constants;
import org.jboss.wsf.stack.cxf.client.UseNewBusFeature;
import org.jboss.wsf.stack.cxf.client.UseTCCLBusFeature;
import org.jboss.wsf.stack.cxf.client.UseThreadBusFeature;

/**
 * A helper class creating a pool of JAXWS clients that invoke the test endpoint.
 * Here each client execution create a new JAXWS services.
 * Returns the number of Bus instances created to run the clients.
 * 
 * @author alessio.soldano@jboss.com
 * @since 04-Oct-2013
 *
 */
public class Helper
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
         strategy = System.getProperty(Constants.JBWS_CXF_JAXWS_CLIENT_BUS_STRATEGY, null);
      }
      final BusCounter busCounter = new BusCounter();
      final ThreadFactory threadFactory = new ThreadFactory()
      {
         private AtomicInteger i = new AtomicInteger(0);
         
         @Override
         public Thread newThread(Runnable r)
         {
            return new Thread(r, "JBWS3373-thread-" + i.getAndIncrement() + "-" + strategy);
         }
      };
      ExecutorService es = Executors.newFixedThreadPool(size, threadFactory);
      List<Client> clients = new ArrayList<Helper.Client>();
      for (int i = 0; i < calls; i++) {
         clients.add(new Client(wsdlURL, feature, busCounter));
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
   
   private static class Client implements Callable<Boolean>
   {
      private final URL wsdlURL;
      private final WebServiceFeature feature;
      private final BusCounter busCounter;

      public Client(final URL wsdlURL, final WebServiceFeature feature, final BusCounter busCounter)
      {
         this.wsdlURL = wsdlURL;
         this.feature = feature;
         this.busCounter = busCounter;
      }

      @Override
      public Boolean call() throws Exception
      {
         QName qname = new QName("http://hello/test", "HelloService");
         Service service = feature != null ? Service.create(wsdlURL, qname, feature) : Service.create(wsdlURL, qname);
         HelloWs helloPort = service.getPort(HelloWs.class);
         org.apache.cxf.endpoint.Client cxfClient = ClientProxy.getClient(helloPort);
         busCounter.count(cxfClient.getBus());
         
         HelloRequest request = new HelloRequest();
         request.setInput("hi");
         HelloResponse response = helloPort.doHello(request);
         return response.getMultiHello().contains("hi");
      }

   }
}
