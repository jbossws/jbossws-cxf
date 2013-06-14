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

import org.apache.cxf.Bus;
import org.apache.cxf.buslifecycle.BusLifeCycleListener;
import org.apache.cxf.buslifecycle.BusLifeCycleManager;
import org.jboss.wsf.stack.cxf.client.util.SpringUtils;
import org.jboss.wsf.stack.cxf.configuration.BusHolder;
import org.jboss.wsf.stack.cxf.configuration.NonSpringBusHolder;
import org.jboss.wsf.stack.cxf.configuration.SpringBusHolder;
import org.jboss.wsf.stack.cxf.metadata.services.DDBeans;
import org.jboss.wsf.test.JBossWSTest;

/**
 * Verifies the Bus is properly shutdown when created through the BusHolder
 * 
 * @author alessio.soldano@jboss.com
 * @since 08-Aug-2010
 *
 */
public class BusHolderLifeCycleTestCase extends JBossWSTest
{
   public void testBusShutdown()
   {
      if (SpringUtils.isSpringAvailable(Thread.currentThread().getContextClassLoader()))
      {
         simpleShutdownTest(new SpringBusHolder(null, null, new URL[]{}));
         shutdownTestWithInnerShutdown(new SpringBusHolder(null, null, new URL[]{}));
         shutdownTestWithNoShutdown(new SpringBusHolder(null, null, new URL[]{}));
      }
      else
      {
         simpleShutdownTest(new NonSpringBusHolder(new DDBeans()));
         shutdownTestWithInnerShutdown(new NonSpringBusHolder(new DDBeans()));
         shutdownTestWithNoShutdown(new NonSpringBusHolder(new DDBeans()));
      }
   }
   
   private static void simpleShutdownTest(BusHolder holder)
   {
      Bus bus = holder.getBus();
      TestLifeCycleListener listener = new TestLifeCycleListener();
      bus.getExtension(BusLifeCycleManager.class).registerLifeCycleListener(listener);
      holder.configure(null, null, null, null);
      holder.close();
      assertEquals("preShutdown method on listener should be called exactly once; number of actual calls: "
                  + listener.getCount(), 1, listener.getCount());
   }
   
   private static void shutdownTestWithInnerShutdown(BusHolder holder)
   {
      Bus bus = holder.getBus();
      TestLifeCycleListener listener = new TestLifeCycleListener();
      bus.getExtension(BusLifeCycleManager.class).registerLifeCycleListener(listener);
      holder.configure(null, null, null, null);
      bus.shutdown(true);
      holder.close();
      assertEquals("preShutdown method on listener should be called exactly once; number of actual calls: "
                  + listener.getCount(), 1, listener.getCount());
   }
   
   private static void shutdownTestWithNoShutdown(BusHolder holder)
   {
      Bus bus = holder.getBus();
      TestLifeCycleListener listener = new TestLifeCycleListener();
      bus.getExtension(BusLifeCycleManager.class).registerLifeCycleListener(listener);
      holder.configure(null, null, null, null);
      assertEquals("preShutdown method on listener shouldn't be called before holder is closed: number of actual calls: "
                  + listener.getCount(), 0, listener.getCount());
      holder.close();
   }

   private static class TestLifeCycleListener implements BusLifeCycleListener
   {
      private volatile int count = 0;

      public int getCount()
      {
         return count;
      }

      @Override
      public void initComplete()
      {
         //NOOP
      }

      @Override
      public void preShutdown()
      {
         count++;
      }

      @Override
      public void postShutdown()
      {
         //NOOP
      }

   }
}
