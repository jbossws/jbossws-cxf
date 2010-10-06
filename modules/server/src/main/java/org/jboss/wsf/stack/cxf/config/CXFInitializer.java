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
package org.jboss.wsf.stack.cxf.config;

import java.util.concurrent.CountDownLatch;

import org.apache.cxf.BusFactory;
import org.jboss.logging.Logger;


/**
 * A bean installed during boot for initializing CXF
 * 
 * @author alessio.soldano@jboss.com
 * @since 06-Opt-2010
 *
 */
public class CXFInitializer
{
   private static Logger logger = Logger.getLogger(CXFInitializer.class);
   private static CountDownLatch defaultBusCDL = new CountDownLatch(1);
   
   public void create() throws Exception
   {
      Thread defaultBusInitThread = new Thread(new DefautBusInitializer(defaultBusCDL), "JBossWS-CXF-DefaultBus-Init");
      defaultBusInitThread.setDaemon(true);
      defaultBusInitThread.start();
   }
   
   public void destroy() throws Exception
   {
      //NOOP
   }
   
   public static void waitForDefaultBusAvailability()
   {
      boolean trace = logger.isTraceEnabled();
      if (trace)
         logger.trace(Thread.currentThread() + " will wait for default bus availability...");
      try
      {
         defaultBusCDL.await();
         if (trace)
            logger.trace("Default bus now available: " + BusFactory.getDefaultBus(false));
      }
      catch (InterruptedException e)
      {
         logger.error("Interrupted while waiting for default bus to be set!");
         throw new RuntimeException(e);
      }
   }

   private class DefautBusInitializer implements Runnable
   {
      private CountDownLatch cdl;

      public DefautBusInitializer(CountDownLatch cdl)
      {
         this.cdl = cdl;
      }

      @Override
      public void run()
      {
         long start = System.currentTimeMillis();
         try
         {
            BusFactory.getDefaultBus();
            if (logger.isTraceEnabled())
            {
               logger.info("Default bus started in " + (System.currentTimeMillis() - start) + " ms by "
                     + Thread.currentThread());
            }
         }
         finally
         {
            cdl.countDown();
         }
      }
   }

}