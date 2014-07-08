/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.jbws3670;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;

public class JBWS3670TestCase extends JBossWSTest
{
   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-cxf-jbws3670.war") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.cxf.jbws3670.HelloWorldService.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.jbws3670.HelloWorldServiceI.class)
               .addClass(org.jboss.test.ws.jaxws.cxf.jbws3670.TestBean.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3670/WEB-INF/jbossws-cxf.xml"), "jbossws-cxf.xml")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/cxf/jbws3670/WEB-INF/web.xml"));
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSCXFTestSetup(JBWS3670TestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
   }
   
   public void testAccess() throws Exception
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-cxf-jbws3670/helloworld?wsdl");
      QName serviceName = new QName("http://jbossws.jboss.org/helloworld", "HelloWorldService");
      Service service = Service.create(wsdlURL, serviceName);
      HelloWorldServiceI port = service.getPort(HelloWorldServiceI.class);
      assertEquals("TestBean is not injected", "Hello jbossws", port.sayHello("jbossws"));
   }
   

   
}
