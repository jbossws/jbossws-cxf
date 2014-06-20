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
package org.jboss.test.ws.jaxws.jbws3026;

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
 * [JBWS-3026] Injecting EJB into Webservice via @EJB(mappedName="MyBean/remote")
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class JBWS3026TestCase extends JBossWSTest
{
   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.JarDeployment("jaxws-jbws3026-ejb.jar") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws3026.MyBean.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3026.MyBeanLocal.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3026.MyBeanRemote.class);
         }
      });
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-jbws3026-web.war") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws3026.MyBeanRemote.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3026.MyService.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3026.MyServiceImpl.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3026/META-INF/permissions.xml"), "permissions.xml")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3026/WEB-INF/web.xml"));
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS3026TestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
   }

   public void testUsecase1WithoutSar() throws Exception
   {
      String endpointAddress = "http://" + getServerHost() + ":8080/jaxws-jbws3026-web/MyService";
      QName serviceName = new QName("http://jbws3026.jaxws.ws.test.jboss.org/", "MyService");
      Service service = Service.create(new URL(endpointAddress + "?wsdl"), serviceName);
      MyService port = (MyService)service.getPort(MyService.class);
      port.useBean();
      port.thisOneWorks();
   }
}
