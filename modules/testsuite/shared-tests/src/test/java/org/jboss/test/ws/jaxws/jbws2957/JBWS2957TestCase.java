/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.test.ws.jaxws.jbws2957;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.test.ws.jaxws.jbws2957.common.HelloIface;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBWS-2957] Tests EJB3 service in web inf lib directory.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public class JBWS2957TestCase extends JBossWSTest
{
   static {
      JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-jbws2957-ejbinwarwebinflib_ejb.jar") { {
         archive
            .addManifest()
            .addClass(org.jboss.test.ws.jaxws.jbws2957.common.HelloIface.class)
            .addClass(org.jboss.test.ws.jaxws.jbws2957.common.HelloImpl.class);
         }
      });
      JBossWSTestHelper.writeToFile(new JBossWSTestHelper.WarDeployment("jaxws-jbws2957-ejbinwarwebinflib_web.war") { {
         archive
            .addManifest()
            .addAsLibrary(new File(JBossWSTestHelper.getTestArchiveDir(), "jaxws-jbws2957-ejbinwarwebinflib_ejb.jar"))
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2957/WEB-INF/ejb-jar.xml"), "ejb-jar.xml")
            .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2957/WEB-INF/wsdl/HelloService.wsdl"), "wsdl/HelloService.wsdl");
         }
      });
      JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-jbws2957-ejbinwarwebinflib.ear") { {
            archive.addManifest().addAsResource(new File(JBossWSTestHelper.getTestArchiveDir(), "jaxws-jbws2957-ejbinwarwebinflib_web.war"));
         }
      });
   }
   
   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS2957TestCase.class, "jaxws-jbws2957-ejbinwarwebinflib.ear");
   }

   public void testEJB() throws Exception
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws2957-ejbinwarwebinflib_web/Service/HelloImpl?wsdl");
      QName serviceName = new QName("http://www.jboss.org/test/ws/jaxws/jbws2957", "Service");
      Service.create(wsdlURL, serviceName);
      Service service = Service.create(wsdlURL, serviceName);
      HelloIface port = (HelloIface)service.getPort(HelloIface.class);
      assertEquals("Hello", port.sayHello());
   }
}
