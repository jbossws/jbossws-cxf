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
package org.jboss.test.ws.jaxws.jbws1822;

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.test.ws.jaxws.jbws1822.webservice.EJB3RemoteIface;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBWS-1822] Cannot find service endpoint target
 *
 * @author richard.opalka@jboss.com
 *
 * @since Jan 8, 2008
 */
public final class JBWS1822TestCase extends JBossWSTest
{
   static {
      JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-jbws1822-shared.jar") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws1822.shared.BeanIface.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1822.shared.BeanImpl.class);
         }
      });
      JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-jbws1822-two-ejb3-inside.jar") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws1822.shared.BeanIface.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1822.shared.BeanImpl.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1822.webservice.EJB3Bean.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1822.webservice.EJB3RemoteIface.class);
         }
      });
      JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-jbws1822-one-ejb3-inside.jar") { {
         archive
               .addManifest()
               .addClass(org.jboss.test.ws.jaxws.jbws1822.webservice.EJB3Bean.class)
               .addClass(org.jboss.test.ws.jaxws.jbws1822.webservice.EJB3RemoteIface.class);
         }
      });
      JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-jbws1822-two-ejb-modules.ear") { {
         archive
               .addManifest()
               .addAsResource(new File(JBossWSTestHelper.getTestArchiveDir(), "jaxws-jbws1822-one-ejb3-inside.jar"))
               .addAsResource(new File(JBossWSTestHelper.getTestArchiveDir(), "jaxws-jbws1822-shared.jar"));
         }
      });
      JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-jbws1822-one-ejb-module.ear") { {
         archive
               .addManifest()
               .addAsResource(new File(JBossWSTestHelper.getTestArchiveDir(), "jaxws-jbws1822-two-ejb3-inside.jar"));
         }
      });
   }

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS1822TestCase.class, "");
   }

   private EJB3RemoteIface getProxy() throws Exception
   {
      QName serviceName = new QName("http://jbossws.org/JBWS1822", "EndpointService");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws1822?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      return (EJB3RemoteIface)service.getPort(EJB3RemoteIface.class);
   }
   
   public void testOneEjbModule() throws Exception
   {
      deploy("jaxws-jbws1822-one-ejb-module.ear");
      try
      {
         assertEquals(getProxy().getMessage(), "Injected hello message");
      }
      finally
      {
         undeploy("jaxws-jbws1822-one-ejb-module.ear");
      }
   }
   
   public void testTwoEjbModules() throws Exception
   {
      deploy("jaxws-jbws1822-two-ejb-modules.ear");
      try
      {
         assertEquals(getProxy().getMessage(), "Injected hello message");
      }
      finally
      {
         undeploy("jaxws-jbws1822-two-ejb-modules.ear");
      }
   }
   
}
