package org.jboss.wsf.stack.cxf.management;

import java.util.Iterator;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import org.apache.cxf.bus.ManagedBus;
import org.apache.cxf.management.jmx.InstrumentationManagerImpl;
import org.jboss.wsf.stack.cxf.Loggers;


/**
 * The InstrumentationManagerImpl extension class to set the JBoss MBeanServer.
 * @author <a herf="mailto:ema@redhat.com> Jim Ma</a>
 * 
 */

public class InstrumentationManagerExtImpl extends InstrumentationManagerImpl
{
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
