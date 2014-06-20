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
package org.jboss.test.ws.jaxws.jbws1813;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * context-root in jboss.xml is ignored
 *
 * http://jira.jboss.org/jira/browse/JBWS-1813
 *
 * @author Thomas.Diesler@jboss.com
 * @since 09-Oct-2007
 */
public class JBWS1813TestCase extends JBossWSTest
{
   public final String TARGET_ENDPOINT_ADDRESS = "http://" + getServerHost() + ":8080/test-context";

   static {
      JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-jbws1813.jar") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws1813.EndpointImpl.class)
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1813/META-INF/jboss-webservices.xml"), "jboss-webservices.xml");
         }
      });
      JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-jbws1813.ear") { {
         archive
               .addManifest()
               .addAsResource(new File(JBossWSTestHelper.getTestArchiveDir(), "jaxws-jbws1813.jar"))
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/jbws1813/META-INF/application.xml"), "application.xml");
         }
      });
   }

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS1813TestCase.class, "jaxws-jbws1813.ear");
   }

   public void testPositive() throws Exception
   {
      URL wsdlURL = new URL(TARGET_ENDPOINT_ADDRESS + "?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/jbws1813", "EndpointService");
      Endpoint port = Service.create(wsdlURL, serviceName).getPort(Endpoint.class);

      String retObj = port.echo("Hello");
      assertEquals("Hello", retObj);
   }
}
