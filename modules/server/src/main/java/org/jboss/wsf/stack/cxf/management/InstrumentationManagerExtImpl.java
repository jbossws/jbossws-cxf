package org.jboss.wsf.stack.cxf.management;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import org.apache.cxf.bus.ManagedBus;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.management.jmx.InstrumentationManagerImpl;


/**
 * The InstrumentationManagerImpl extension class to set the JBoss MBeanServer.
 * @author <a herf="mailto:ema@redhat.com> Jim Ma</a>
 * 
 */

public class InstrumentationManagerExtImpl extends InstrumentationManagerImpl
{
   private static final Logger LOG = LogUtils.getL7dLogger(InstrumentationManagerExtImpl.class);
   private MBeanServer mbeanServer = null;

   
   /**
    * TODO: to see if this can be moved to cxf code base 
    */
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
         LOG.log(Level.SEVERE, "Register bus " + this.getBus() + " failure :" + e.getMessage());
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
