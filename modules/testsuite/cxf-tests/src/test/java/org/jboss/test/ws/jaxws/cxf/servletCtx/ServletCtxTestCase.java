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
package org.jboss.test.ws.jaxws.cxf.servletCtx;

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
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author alessio.soldano@jboss.com
 * @since 09-Jul-2013
 */
@RunWith(Arquillian.class)
public class ServletCtxTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-cxf-servletCtx.war");
      archive.setManifest(new StringAsset("Manifest-Version: 1.0\n"
               + "Dependencies: org.jboss.logging\n"))
         .addClass(org.jboss.test.ws.jaxws.cxf.servletCtx.EndpointOne.class)
         .addClass(org.jboss.test.ws.jaxws.cxf.servletCtx.EndpointOneImpl.class);
      return archive;
   }

   private final String targetNS = "http://org.jboss.ws.jaxws.cxf/servletCtx";

   private EndpointOne portOne;

   private final int defaultSize = 30;

   @Test
   @RunAsClient
   public void testAccess() throws Exception
   {
      initPort();
      int count1 = portOne.getCount1();
      int count2 = portOne.getCount2();
      Object retObj = portOne.echo("Hello");
      assertEquals("Hello", retObj);
      assertEquals(1, portOne.getCount1() - count1);
      assertEquals(1, portOne.getCount2() - count2);
      count1 = portOne.getCount1();
      count2 = portOne.getCount2();
      portOne.echoOneWay("Hi");
      Thread.sleep(3000);
      assertEquals(1, portOne.getCount1() - count1);
      assertEquals(1, portOne.getCount2() - count2);
   }

   @Test
   @RunAsClient
   public void testConcurrentInvocations() throws Exception
   {
      runConcurrentTests(false);
   }

   @Test
   @RunAsClient
   public void testConcurrentOneWayInvocations() throws Exception
   {
      runConcurrentTests(true);
   }

   private void runConcurrentTests(boolean oneway) throws Exception
   {
      initPort();
      final int size = defaultSize;
      int count1 = portOne.getCount1();
      int count2 = portOne.getCount2();
      ExecutorService es = Executors.newFixedThreadPool(size);
      List<Callable<Boolean>> callables = new ArrayList<Callable<Boolean>>(size);
      for (int i = 0; i < size; i++)
      {
         callables.add(new CallableOne(portOne, oneway, i));
      }
      List<Future<Boolean>> futures = es.invokeAll(callables);
      for (Future<Boolean> f : futures)
      {
         assertTrue(f.get());
      }
      if (oneway)
      {
         Thread.sleep(3000);
      }
      assertEquals(size, portOne.getCount1() - count1);
      assertEquals(size, portOne.getCount2() - count2);
   }

   private void initPort() throws MalformedURLException
   {
      URL wsdlOneURL = new URL(baseURL + "/ServiceOne?wsdl");
      QName serviceOneName = new QName(targetNS, "ServiceOne");
      Service serviceOne = Service.create(wsdlOneURL, serviceOneName);
      portOne = (EndpointOne) serviceOne.getPort(EndpointOne.class);
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