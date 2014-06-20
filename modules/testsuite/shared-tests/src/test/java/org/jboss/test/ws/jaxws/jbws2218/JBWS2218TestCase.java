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
package org.jboss.test.ws.jaxws.jbws2218;

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
 * [JBWS-2218] @WebContext url pattern problem
 *
 * @author richard.opalka@jboss.com
 */
public class JBWS2218TestCase extends JBossWSTest
{
   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.JarDeployment("jaxws-jbws2218.jar") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws2218.EJB3Bean.class)
               .addClass(org.jboss.test.ws.jaxws.jbws2218.EndpointInterface.class);
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS2218TestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
   }

   public void testSimpleAccess() throws Exception
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/myweb/myweb-service/WebDelegateEndPoint?wsdl");
      QName serviceName = new QName("http://jbws2218.jaxws.ws.test.jboss.org/", "EJB3BeanService");
      Service service = Service.create(wsdlURL, serviceName);
      EndpointInterface port = service.getPort(EndpointInterface.class);
      String hello = port.helloSimple("hello");
      assertEquals("hello", hello);
   }
}
