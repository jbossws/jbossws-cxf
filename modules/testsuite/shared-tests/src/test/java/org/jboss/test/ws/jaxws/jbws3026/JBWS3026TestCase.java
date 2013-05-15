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

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBWS-3026] Injecting EJB into Webservice via @EJB(mappedName="MyBean/remote")
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class JBWS3026TestCase extends JBossWSTest
{
   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS3026TestCase.class, "jaxws-jbws3026-ejb.jar,jaxws-jbws3026-web.war");
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
