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
import javax.xml.ws.Service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * [JBWS-3497] Add ability to configure the queue depth on the asynchronous (@Oneway) work queue.
 * This testcase basically verifies the initial workaround for the issue works.
 * 
 * @author alessio.soldano@jboss.com
 *
 */
@RunWith(Arquillian.class)
public class JBWS3497TestCase extends JBossWSTest
{
   private EndpointOne portOne;
   
   protected int defaultSize = 200;

    @Deployment(testable = false)
    public static WebArchive createDeployments() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class,"jaxws-cxf-jbws3497.war");
         archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: org.apache.cxf,org.jboss.ws.cxf.jbossws-cxf-server\n"))
               .addClass(org.jboss.test.ws.jaxws.cxf.jbws3497.EndpointOne.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.jbws3497.EndpointOneImpl.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3497/WEB-INF/jbossws-cxf.xml"), "jbossws-cxf.xml")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3497/WEB-INF/cxf.xml"), "classes/cxf.xml")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3497/WEB-INF/web.xml"));
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
   //Manually enable this test if willing to check (and try modifying 'queueSize' value in cxf.xml); otherwise the EndpointOne
   //implementation simply checks the related AutomaticWorkQueue is properly configured. 
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
      URL wsdlOneURL = new URL("http://" + getServerHost() + ":" + getServerPort() + "/jaxws-cxf-jbws3497/ServiceOne/EndpointOne?wsdl");
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
