/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws3753;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestHelper.BaseDeployment;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBWS-3753] Improve destination matching when processing requests
 *
 * @author alessio.soldano@jboss.com
 */
public class JBWS3753TestCase extends JBossWSTest
{
   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-jbws3753.war") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws3753.ServiceAImpl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3753.ServiceBImpl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3753.ServiceImpl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3753.ServiceInterface.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3753/WEB-INF/web.xml"));
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite() {
      return new JBossWSTestSetup(JBWS3753TestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
   }
   
   public void testService() throws Exception
   {
      Service service = Service.create(new URL("http://" + getServerHost() + ":8080/jaxws-jbws3753/service?wsdl"), new QName("http://org.jboss.ws/jaxws/jbws3753/", "MyService"));
      ServiceInterface port = service.getPort(ServiceInterface.class);
      assertEquals("Hi John", port.greetMe("John"));
   }
   
   public void testServiceA() throws Exception
   {
      Service service = Service.create(new URL("http://" + getServerHost() + ":8080/jaxws-jbws3753/serviceA?wsdl"), new QName("http://org.jboss.ws/jaxws/jbws3753/", "MyService"));
      ServiceInterface port = service.getPort(ServiceInterface.class);
      assertEquals("(A) Hi John", port.greetMe("John"));
   }

   public void testServiceB() throws Exception
   {
      Service service = Service.create(new URL("http://" + getServerHost() + ":8080/jaxws-jbws3753/serviceB?wsdl"), new QName("http://org.jboss.ws/jaxws/jbws3753/", "MyService"));
      ServiceInterface port = service.getPort(ServiceInterface.class);
      assertEquals("(B) Hi John", port.greetMe("John"));
   }

}
