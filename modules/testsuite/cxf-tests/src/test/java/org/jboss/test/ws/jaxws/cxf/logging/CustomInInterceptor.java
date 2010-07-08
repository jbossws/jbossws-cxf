package org.jboss.test.ws.jaxws.cxf.logging;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.message.Message;

public class CustomInInterceptor extends LoggingInInterceptor
{
   private static volatile int count = 0;
   
   public void handleMessage(Message message) throws Fault {
      count++;
      super.handleMessage(message);
   }
   
   public static int getCount()
   {
      System.out.println("COUNT = "+count);
      return count;
   }
}
