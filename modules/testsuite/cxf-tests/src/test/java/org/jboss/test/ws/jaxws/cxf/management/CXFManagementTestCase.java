package org.jboss.test.ws.jaxws.cxf.management;

import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;

public class CXFManagementTestCase extends JBossWSTest
{

   public static Test suite()
   {
      return new JBossWSCXFTestSetup(CXFManagementTestCase.class, "jaxws-cxf-management.war");
   }

   public void testJMXBean() throws Exception {
      MBeanServerConnection server = getServer();
      ObjectName name = new ObjectName("org.apache.cxf:*");
      Set<?> cxfBeans = server.queryMBeans(name, null);
      assertTrue(cxfBeans.size() > 0);
   }

}
