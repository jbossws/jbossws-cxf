/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.jboss.test.ws.jaxws.jbws3367;

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
 * [JBWS-3367][AS7-1605] jboss-web.xml ignored for web service root
 *
 * This test case tests if only EJB3 endpoints are in war archive.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public class OnlyEjbInWarTestCase extends JBossWSTest
{
   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-jbws3367-usecase2.war") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws3367.EJB3Endpoint.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3367.EndpointIface.class)
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws3367/WEB-INF-2/jboss-web.xml"), "jboss-web.xml");
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSTestSetup(OnlyEjbInWarTestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
   }

   public void testEJB3Endpoint() throws Exception
   {
      final QName serviceName = new QName("org.jboss.test.ws.jaxws.jbws3367", "EJB3EndpointService");
      final URL wsdlURL = new URL("http://" + getServerHost() +  ":8080/jbws3367-customized2/EJB3Endpoint?wsdl");
      final Service service = Service.create(wsdlURL, serviceName);
      final EndpointIface port = service.getPort(EndpointIface.class);
      final String result = port.echo("hello");
      assertEquals("EJB3 hello", result);
   }

}
