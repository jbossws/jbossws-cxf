package org.jboss.test.ws.jaxws.cxf.management;

import java.io.File;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class CXFManagementTestCase extends JBossWSTest
{
   @Deployment(testable = false)
   public static WebArchive createDeployment() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-cxf-management.war");
      archive
            .addManifest()
            .addClass(org.jboss.test.ws.jaxws.cxf.management.HelloWorld.class)
            .addClass(org.jboss.test.ws.jaxws.cxf.management.HelloWorldImpl.class)
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/management/WEB-INF/jboss-webservices.xml"), "jboss-webservices.xml")
            .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/management/WEB-INF/web.xml"));
      return archive;
   }

   @Test
   @RunAsClient
   public void testJMXBean() throws Exception {
      MBeanServerConnection server = getServer();
      ObjectName name = new ObjectName("org.apache.cxf:*");
      Set<?> cxfBeans = server.queryMBeans(name, null);
      assertTrue(cxfBeans.size() > 0);
   }

}
