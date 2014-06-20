/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws2486;

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
 * [JBWS-2486] POJO service should be shared
 *
 * @author richard.opalka@jboss.com
 */
public class JBWS2486TestCase extends JBossWSTest
{
   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-jbws2486.war") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws2486.JBWS2486.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2486.JBWS2486Impl.class)
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws2486/WEB-INF/web.xml"));
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS2486TestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
   }

   public void testIssue() throws Exception
   {
      QName serviceName = new QName("http://jbws2486.jaxws.ws.test.jboss.org/", "JBWS2486Service");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws2486/JBWS2486Service?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      JBWS2486 proxy = (JBWS2486)service.getPort(JBWS2486.class);
      
      String serviceInstanceId = proxy.getServiceInstanceId();
      for (int i = 1; i <= 10; i++)
      {
         assertEquals(proxy.getServiceInstanceId(), serviceInstanceId);
      }
   }

}
