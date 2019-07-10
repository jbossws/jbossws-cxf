package org.jboss.test.ws.jaxws.cxf.interceptors;
import java.util.HashMap;
import java.util.Map;

import org.apache.cxf.logging.FaultListener;
import org.apache.cxf.message.Message;
public class JBossWSFaultListener implements FaultListener
{
   public static Map<String, Exception> exceptions = new HashMap<String, Exception>();
   @Override
   public boolean faultOccurred(Exception exception, String description, Message message)
   {
      exceptions.put(description, exception);
      return true;
   }

}
