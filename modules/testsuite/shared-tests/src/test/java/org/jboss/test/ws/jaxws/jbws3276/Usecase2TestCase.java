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

package org.jboss.test.ws.jaxws.jbws3276;

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
 * [JBWS-3276] Tests anonymous POJO in web archive that is missing web.xml.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public class Usecase2TestCase extends JBossWSTest
{
   public static BaseDeployment<?>[] createDeployments() {
      List<BaseDeployment<?>> list = new LinkedList<BaseDeployment<?>>();
      list.add(new JBossWSTestHelper.WarDeployment("jaxws-jbws3276-usecase2.war") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws3276.AnonymousPOJO.class)
               .addClass(org.jboss.test.ws.jaxws.jbws3276.POJOIface.class);
         }
      });
      return list.toArray(new BaseDeployment<?>[list.size()]);
   }

   public static Test suite()
   {
      return new JBossWSTestSetup(Usecase2TestCase.class, JBossWSTestHelper.writeToFile(createDeployments()));
   }

   public void testAnonymousEndpoint() throws Exception
   {
      final QName serviceName = new QName("org.jboss.test.ws.jaxws.jbws3276", "AnonymousPOJOService");
      final URL wsdlURL = new URL("http://" + getServerHost() +  ":8080/jaxws-jbws3276-usecase2/AnonymousPOJOService?wsdl");
      final Service service = Service.create(wsdlURL, serviceName);
      final POJOIface port = service.getPort(POJOIface.class);
      final String result = port.echo("hello");
      assertEquals("hello from anonymous POJO", result);
   }

}
