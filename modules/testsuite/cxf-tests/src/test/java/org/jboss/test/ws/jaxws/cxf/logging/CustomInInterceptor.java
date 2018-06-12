package org.jboss.test.ws.jaxws.cxf.logging;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.message.Message;

public class CustomInInterceptor extends LoggingInInterceptor
{
   private static AtomicInteger count = new AtomicInteger(0);
   
   public void handleMessage(Message message) throws Fault {
      count.incrementAndGet();
      super.handleMessage(message);
   }
   
   public static int getCount()
   {
      System.out.println("COUNT = "+count.get());
      return count.get();
   }
}
