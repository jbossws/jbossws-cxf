package org.jboss.test.ws.jaxws.cxf.management;

import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

public class CXFManagementTestCase extends JBossWSTest
{
   public static Test suite()
   {
      return new JBossWSTestSetup(CXFManagementTestCase.class, "jaxws-cxf-management.war");
   }
   
   public void testJMXBean() throws Exception {
      InitialContext context = new InitialContext();
      MBeanServerConnection server = (MBeanServerConnection)context.lookup("jmx/invoker/RMIAdaptor");
      ObjectName name = new ObjectName("org.apache.cxf:*");
      Set cxfBeans = server.queryMBeans(name, null);
      assertTrue(cxfBeans.size() > 0); 
   }

}
