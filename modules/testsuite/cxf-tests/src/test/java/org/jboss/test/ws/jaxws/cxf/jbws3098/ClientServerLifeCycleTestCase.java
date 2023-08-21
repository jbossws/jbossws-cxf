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
package org.jboss.test.ws.jaxws.cxf.jbws3098;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.ClientLifeCycleListener;
import org.apache.cxf.endpoint.ClientLifeCycleManager;
import org.apache.cxf.endpoint.ServerLifeCycleManager;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.stack.cxf.client.UseThreadBusFeature;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Verifies the Bus is properly configured with Client/Server LifeCycleManager instances
 *
 * @author alessio.soldano@jboss.com
 * @since 08-Aug-2010
 */
@RunWith(Arquillian.class)
public class ClientServerLifeCycleTestCase extends JBossWSTest
{
   @ArquillianResource
   private URL baseURL;
   
   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-cxf-jbws3098.war");
      archive.addManifest()
            .addClass(org.jboss.test.ws.jaxws.cxf.jbws3098.EndpointOneImpl.class)
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3098/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testClientLifeCycleManager()
   {
      Bus bus = BusFactory.newInstance().createBus();
      try {
         assertNotNull("Cannot find ClientLifeCycleManager impl in current bus", bus.getExtension(ClientLifeCycleManager.class));
      } finally {
         bus.shutdown(true);
      }
   }

   @Test
   @RunAsClient
   public void testServerLifeCycleManager()
   {
      Bus bus = BusFactory.newInstance().createBus();
      try {
         assertNotNull("Cannot find ServerLifeCycleManager impl in current bus", bus.getExtension(ServerLifeCycleManager.class));
      } finally {
         bus.shutdown(true);
      }
   }

   @Test
   @RunAsClient
   public void testCustomClientLifeCycleListener() throws Exception
   {
      Bus bus = BusFactory.newInstance().createBus();
      BusFactory.setThreadDefaultBus(bus);
      try {
         URL wsdlOneURL = new URL(baseURL + "/ServiceOne/EndpointOne?wsdl");
         QName serviceOneName = new QName("http://org.jboss.ws.jaxws.cxf/jbws3098", "ServiceOne");
         Service serviceOne = Service.create(wsdlOneURL, serviceOneName, new UseThreadBusFeature());
         CustomClientLifeCycleListener listener = new CustomClientLifeCycleListener();
         ClientLifeCycleManager mgr = bus.getExtension(ClientLifeCycleManager.class);
         try {
            mgr.registerListener(listener);
            assertEquals(0, listener.getCount());
            EndpointOne portOne = (EndpointOne)serviceOne.getPort(EndpointOne.class);
            assertEquals(1, listener.getCount());
            assertEquals("Foo", portOne.echo("Foo"));
         } finally {
            mgr.unRegisterListener(listener);
         }
      } finally {
         bus.shutdown(true);
      }
   }

   private class CustomClientLifeCycleListener implements ClientLifeCycleListener
   {
      private volatile int count = 0;

      public int getCount()
      {
         return count;
      }

      @Override
      public void clientCreated(Client client)
      {
         count++;
      }

      @Override
      public void clientDestroyed(Client client)
      {
         //NOOP
      }
   }
}
