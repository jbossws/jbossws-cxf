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
package org.jboss.wsf.stack.cxf.management;

import java.util.Iterator;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.ManagedBus;
import org.apache.cxf.buslifecycle.BusLifeCycleManager;
import org.apache.cxf.management.InstrumentationManager;
import org.apache.cxf.management.jmx.InstrumentationManagerImpl;
import org.jboss.wsf.stack.cxf.i18n.Loggers;


/**
 * The InstrumentationManagerImpl extension class to set the JBoss MBeanServer.
 * @author <a herf="mailto:ema@redhat.com> Jim Ma</a>
 * 
 */

public class InstrumentationManagerExtImpl extends InstrumentationManagerImpl
{
   private MBeanServer mbeanServer = null;

   @Override
   public void init()
   {
      Bus bus = getBus();
      if (null != bus)
      {
         bus.setExtension(this, InstrumentationManager.class);
         BusLifeCycleManager blcm = bus.getExtension(BusLifeCycleManager.class);
         if (null != blcm)
         {
            blcm.registerLifeCycleListener(this);
         }
      }
   }
   public void initMBeanServer()
   {
      this.setServer(this.getJBossMbeanServer());

      ManagedBus mbus = new ManagedBus(this.getBus());
      try
      {
         register(mbus);
      }
      catch (JMException e)
      {
         Loggers.ROOT_LOGGER.errorRegisteringBus(this.getBus(), e);
      }

   }

   protected MBeanServer getJBossMbeanServer()
   {

      if (mbeanServer == null)
      {
         for (Iterator<MBeanServer> i = MBeanServerFactory.findMBeanServer(null).iterator(); i.hasNext();)
         {
            mbeanServer = i.next();
            if (mbeanServer.getClass().getName().startsWith("org.jboss"))
            {
               break;
            }
         }
      }
      return mbeanServer;
   }
}
