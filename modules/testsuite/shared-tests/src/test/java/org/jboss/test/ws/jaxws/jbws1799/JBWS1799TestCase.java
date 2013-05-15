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
package org.jboss.test.ws.jaxws.jbws1799;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBWS-1799] Two ejb3s exposed as 2 different web services in the same ear file.
 * Can't have same methods with different parameters in two separate EJBs.
 *
 * @author richard.opalka@jboss.com
 *
 * @since Oct 8, 2007
 */
public class JBWS1799TestCase extends JBossWSTest
{
   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS1799TestCase.class, "jaxws-jbws1799.jar");
   }

   public void testFirstService() throws Exception
   {
      QName serviceName = new QName("namespace1", "UserAccountService1.0");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/svc-useracctv1.0/UserAccountService1.0?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      IUserAccountService proxy = (IUserAccountService)service.getPort(IUserAccountService.class);

      assertTrue(proxy.authenticate("authorized"));
      assertFalse(proxy.authenticate("unauthorized"));
   }

   public void testSecondService() throws Exception
   {
      QName serviceName = new QName("namespaceExt", "UserAccountServiceExt1.0");
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/svc-useracctv1.0/UserAccountServiceExt1.0?wsdl");

      Service service = Service.create(wsdlURL, serviceName);
      IUserAccountServiceExt proxy = (IUserAccountServiceExt)service.getPort(IUserAccountServiceExt.class);

      assertTrue(proxy.authenticate("authorized", "password"));
      assertFalse(proxy.authenticate("unauthorized", "password"));
   }
}
