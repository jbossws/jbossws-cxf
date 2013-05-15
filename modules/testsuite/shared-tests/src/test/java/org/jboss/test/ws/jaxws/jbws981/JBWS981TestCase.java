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
package org.jboss.test.ws.jaxws.jbws981;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBWS-981] Virtual host configuration for EJB endpoints
 *
 * @author darran.lofthouse@jboss.com
 * @since Nov 2, 2006
 */
public class JBWS981TestCase extends JBossWSTest
{

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS981TestCase.class, "jaxws-jbws981.jar");
   }

   public void testCall() throws Exception
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws981/EndpointService/EJB3Bean?wsdl");
      QName serviceName = new QName("http://www.jboss.org/test/ws/jaxws/jbws981", "EndpointService");
      Service.create(wsdlURL, serviceName);
      Service service = Service.create(wsdlURL, serviceName);
      EndpointInterface port = (EndpointInterface)service.getPort(EndpointInterface.class);
      
      String message = "Web service mapped to virtual host.";
      assertEquals("Web service mapped to virtual host.", port.hello(message));
   }
}
