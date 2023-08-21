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
package org.jboss.test.ws.jaxws.cxf.jbws3497;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-3497] Add ability to configure the queue depth on the asynchronous (@Oneway) work queue.
 * 
 * @author alessio.soldano@jboss.com
 *
 */
@RunWith(Arquillian.class)
public class JBWS3497TestCase extends JBossWSTest
{
   private EndpointOne portOne;
   
   protected int defaultSize = 200;
   
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static JavaArchive createDeployment() {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "jaxws-cxf-jbws3497.jar");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
                  + "Dependencies: org.apache.cxf,org.jboss.ws.cxf.jbossws-cxf-server\n"))
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3497.EndpointOne.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3497.EndpointOneImpl.class)
            .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3497/META-INF/jboss-webservices.xml"), "jboss-webservices.xml");
      return archive;
   }

   @Test
   @RunAsClient
   public void testAccess() throws Exception
   {
      initPorts();
      int count1 = portOne.getCount();
      Object retObj = portOne.echo("Hello");
      assertEquals("Hello", retObj);
      assertEquals(1, portOne.getCount() - count1);
   }
   
   //Disabled as there's no easy way for knowing the server is logging WARN messages saying the @OneWay processing queue is full.
   //Manually enable this test if willing to check (and try modifying 'cxf.queue.default.maxQueueSize' prop value in jboss-webservices.xml);
   //otherwise the EndpointOne implementation simply checks the related AutomaticWorkQueue is properly configured. 
   public void _testConcurrentOneWayInvocations() throws Exception
   {
      runConcurrentTests(true);
   }
   
   private void runConcurrentTests(boolean oneway) throws Exception
   {
      initPorts();
      final int size = defaultSize;
      int count1 = portOne.getCount();
      ExecutorService es = Executors.newFixedThreadPool(size);
      List<Callable<Boolean>> callables = new ArrayList<Callable<Boolean>>(size*3);
      for (int i = 0; i < size*3; i++)
      {
         callables.add(new CallableOne(portOne, oneway, i));
      }
      List<Future<Boolean>> futures = es.invokeAll(callables);
      for (Future<Boolean> f : futures)
      {
         assertTrue(f.get());
      }
      if (oneway) {
         Thread.sleep(3000);
      }
      assertEquals(size*3, portOne.getCount() - count1);
   }
   
   private void initPorts() throws MalformedURLException
   {
      URL wsdlOneURL = new URL(baseURL + "/jaxws-cxf-jbws3497/ServiceOne/EndpointOne?wsdl");
      QName serviceOneName = new QName("http://org.jboss.ws.jaxws.cxf/jbws3497", "ServiceOne");
      Service serviceOne = Service.create(wsdlOneURL, serviceOneName);
      portOne = (EndpointOne)serviceOne.getPort(EndpointOne.class);
   }
   
   private static class CallableOne implements Callable<Boolean>
   {
      private EndpointOne port;
      private boolean oneway;
      private int seqNum;
      
      public CallableOne(EndpointOne port, boolean oneway, int seqNum)
      {
         this.port = port;
         this.oneway = oneway;
         this.seqNum = seqNum;
      }
      
      public Boolean call() throws Exception
      {
         String arg = "Foo" + seqNum;
         if (oneway)
         {
            port.echoOneWay(arg);
            return true;
         }
         else
         {
            String result = port.echo(arg);
            return arg.equals(result);
         }
      }
   }
   
}
