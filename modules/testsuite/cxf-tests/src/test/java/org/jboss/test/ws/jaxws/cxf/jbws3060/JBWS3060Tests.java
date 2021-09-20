/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.jbws3060;

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

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Test;

/**
 *
 * @author alessio.soldano@jboss.com
 * @since 11-Jun-2010
 */
public abstract class JBWS3060Tests extends JBossWSTest //*Tests does not match the configured surefire filter on test classes' names
{
   private String targetNS = "http://org.jboss.ws.jaxws.cxf/jbws3060";
   
   private EndpointOne portOne;
   private EndpointTwo portTwo;
   
   protected int defaultSize = 30;
   
   protected abstract String getEndpointOneURL();

   protected abstract String getEndpointTwoURL();

   @Test
   @RunAsClient
   public void testAccess() throws Exception
   {
      initPorts();
      int count1 = portOne.getCount();
      int count2 = portTwo.getCount();
      Object retObj = portOne.echo("Hello");
      assertEquals("Hello", retObj);
      retObj = portTwo.sayHello("John");
      assertEquals("Hi John", retObj);
      assertEquals(1, portOne.getCount() - count1);
      assertEquals(1, portTwo.getCount() - count2);
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
      initPorts();
      final int size = defaultSize;
      int count1 = portOne.getCount();
      int count2 = portTwo.getCount();
      ExecutorService es = Executors.newFixedThreadPool(size*2);
      List<Callable<Boolean>> callables = new ArrayList<Callable<Boolean>>(size*2);
      for (int i = 0; i < size; i++)
      {
         callables.add(new CallableOne(portOne, oneway, i));
         callables.add(new CallableTwo(portTwo, oneway, i));
      }
      List<Future<Boolean>> futures = es.invokeAll(callables);
      for (Future<Boolean> f : futures)
      {
         assertTrue(f.get());
      }
      if (oneway) {
         Thread.sleep(3000);
      }
      assertEquals(size, portOne.getCount() - count1);
      assertEquals(size, portTwo.getCount() - count2);
   }
   
   private void initPorts() throws MalformedURLException
   {
      URL wsdlOneURL = new URL(getEndpointOneURL() + "?wsdl");
      QName serviceOneName = new QName(targetNS, "ServiceOne");
      Service serviceOne = Service.create(wsdlOneURL, serviceOneName);
      portOne = (EndpointOne)serviceOne.getPort(EndpointOne.class);
      
      URL wsdlTwoURL = new URL(getEndpointTwoURL() + "?wsdl");
      QName serviceTwoName = new QName(targetNS, "ServiceTwo");
      Service serviceTwo = Service.create(wsdlTwoURL, serviceTwoName);
      portTwo = (EndpointTwo)serviceTwo.getPort(EndpointTwo.class);
   }
   
   private static class CallableOne implements Callable<Boolean>
   {
      private final EndpointOne port;
      private final boolean oneway;
      private final int seqNum;
      
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
   
   private static class CallableTwo implements Callable<Boolean>
   {
      private final EndpointTwo port;
      private final boolean oneway;
      private final int seqNum;
      
      public CallableTwo(EndpointTwo port, boolean oneway, int seqNum)
      {
         this.port = port;
         this.oneway = oneway;
         this.seqNum = seqNum;
      }
      
      public Boolean call() throws Exception
      {
         String arg = "John" + seqNum;
         if (oneway)
         {
            port.sayHelloOneWay(arg);
            return true;
         }
         else
         {
            String result = port.sayHello(arg);
            return ("Hi " + arg).equals(result);
         }
      }
   }
}
