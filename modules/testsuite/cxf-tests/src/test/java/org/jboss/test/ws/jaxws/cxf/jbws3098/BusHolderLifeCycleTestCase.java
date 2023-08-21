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

import org.apache.cxf.Bus;
import org.apache.cxf.buslifecycle.BusLifeCycleListener;
import org.apache.cxf.buslifecycle.BusLifeCycleManager;
import org.jboss.ws.common.deployment.DefaultDeploymentModelFactory;
import org.jboss.ws.common.management.AbstractServerConfig;
import org.jboss.wsf.spi.deployment.Deployment;
import org.jboss.wsf.spi.management.ServerConfig;
import org.jboss.wsf.spi.metadata.config.SOAPAddressRewriteMetadata;
import org.jboss.wsf.stack.cxf.configuration.BusHolder;
import org.jboss.wsf.stack.cxf.metadata.services.DDBeans;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Test;

/**
 * Verifies the Bus is properly shutdown when created through the BusHolder
 * 
 * @author alessio.soldano@jboss.com
 * @since 08-Aug-2010
 *
 */
public class BusHolderLifeCycleTestCase extends JBossWSTest
{
   @Test
   public void testBusShutdown()
   {
      simpleShutdownTest(new BusHolder(new DDBeans()));
      shutdownTestWithInnerShutdown(new BusHolder(new DDBeans()));
      shutdownTestWithNoShutdown(new BusHolder(new DDBeans()));
   }
   
   private static void simpleShutdownTest(BusHolder holder)
   {
      Bus bus = holder.getBus();
      TestLifeCycleListener listener = new TestLifeCycleListener();
      bus.getExtension(BusLifeCycleManager.class).registerLifeCycleListener(listener);
      Deployment dep = new DefaultDeploymentModelFactory().newDeployment("testDeployment", null, null);
      dep.addAttachment(SOAPAddressRewriteMetadata.class, new SOAPAddressRewriteMetadata(getTestServerConfig(), null));
      holder.configure(null, null, null, dep);
      holder.close();
      assertEquals("preShutdown method on listener should be called exactly once; number of actual calls: "
                  + listener.getCount(), 1, listener.getCount());
   }
   
   private static void shutdownTestWithInnerShutdown(BusHolder holder)
   {
      Bus bus = holder.getBus();
      TestLifeCycleListener listener = new TestLifeCycleListener();
      bus.getExtension(BusLifeCycleManager.class).registerLifeCycleListener(listener);
      Deployment dep = new DefaultDeploymentModelFactory().newDeployment("testDeployment", null, null);
      dep.addAttachment(SOAPAddressRewriteMetadata.class, new SOAPAddressRewriteMetadata(getTestServerConfig(), null));
      holder.configure(null, null, null, dep);
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
      Deployment dep = new DefaultDeploymentModelFactory().newDeployment("testDeployment", null, null);
      dep.addAttachment(SOAPAddressRewriteMetadata.class, new SOAPAddressRewriteMetadata(getTestServerConfig(), null));
      holder.configure(null, null, null, dep);
      assertEquals("preShutdown method on listener shouldn't be called before holder is closed: number of actual calls: "
                  + listener.getCount(), 0, listener.getCount());
      holder.close();
   }
   
   private static ServerConfig getTestServerConfig() {
      return new AbstractServerConfig()
      {
         @Override
         public File getServerTempDir()
         {
            // TODO Auto-generated method stub
            return null;
         }
         @Override
         public File getServerDataDir()
         {
            // TODO Auto-generated method stub
            return null;
         }
         @Override
         public File getHomeDir()
         {
            // TODO Auto-generated method stub
            return null;
         }
      };
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
