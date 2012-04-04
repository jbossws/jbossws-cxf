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
package org.jboss.test.ws.jaxws.cxf.jbws3098;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.ClientLifeCycleListener;
import org.apache.cxf.endpoint.ClientLifeCycleManager;
import org.apache.cxf.endpoint.ServerLifeCycleManager;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;

/**
 * Verifies the Bus is properly configured with Client/Server LifeCycleManager instances
 *
 * @author alessio.soldano@jboss.com
 * @since 08-Aug-2010
 */
public class ClientServerLifeCycleTestCase extends JBossWSTest
{
   private String endpointOneURL = "http://" + getServerHost() + ":8080/jaxws-cxf-jbws3098/ServiceOne/EndpointOne";
   private String targetNS = "http://org.jboss.ws.jaxws.cxf/jbws3098";

   public static Test suite()
   {
      return new JBossWSCXFTestSetup(ClientServerLifeCycleTestCase.class, "jaxws-cxf-jbws3098.war");
   }

   public void testClientLifeCycleManager()
   {
      Bus bus = BusFactory.newInstance().createBus();
      assertNotNull("Cannot find ClientLifeCycleManager impl in current bus", bus.getExtension(ClientLifeCycleManager.class));
      bus.shutdown(true);
   }

   public void testServerLifeCycleManager()
   {
      Bus bus = BusFactory.newInstance().createBus();
      assertNotNull("Cannot find ServerLifeCycleManager impl in current bus", bus.getExtension(ServerLifeCycleManager.class));
      bus.shutdown(true);
   }

   public void testCustomClientLifeCycleListener() throws Exception
   {
      URL wsdlOneURL = new URL(endpointOneURL + "?wsdl");
      QName serviceOneName = new QName(targetNS, "ServiceOne");
      Service serviceOne = Service.create(wsdlOneURL, serviceOneName);
      Bus bus = BusFactory.getThreadDefaultBus(false);
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
