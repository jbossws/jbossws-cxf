package org.jboss.test.ws.jaxws.cxf.management;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;

public class CXFManagementTestCase extends JBossWSTest
{
   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-cxf-management.war") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.cxf.management.HelloWorld.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.management.HelloWorldImpl.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/management/WEB-INF/jboss-webservices.xml"), "jboss-webservices.xml")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/management/WEB-INF/web.xml"));
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSCXFTestSetup(CXFManagementTestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
   }

   public void testJMXBean() throws Exception {
      MBeanServerConnection server = getServer();
      ObjectName name = new ObjectName("org.apache.cxf:*");
      Set<?> cxfBeans = server.queryMBeans(name, null);
      assertTrue(cxfBeans.size() > 0);
   }

}
