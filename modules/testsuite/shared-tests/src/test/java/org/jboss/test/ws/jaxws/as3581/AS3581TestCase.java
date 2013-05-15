/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.jboss.test.ws.jaxws.as3581;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [AS7-3581] Tests manual JNDI lookup in @Oneway annotated method.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public class AS3581TestCase extends JBossWSTest
{

   public static Test suite()
   {
      return new JBossWSTestSetup(AS3581TestCase.class, "jaxws-as3581.war");
   }

   public void testEndpoint() throws Exception
   {
      // test one-way scenario
      final QName serviceName = new QName("org.jboss.test.ws.jaxws.as3581", "SimpleService");
      final URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-as3581/SimpleService?wsdl");
      final Service service = Service.create(wsdlURL, serviceName);
      final EndpointIface port = service.getPort(EndpointIface.class);
      port.doit();
      // test req-resp scenario
      final QName serviceName2 = new QName("org.jboss.test.ws.jaxws.as3581", "SimpleService2");
      final URL wsdlURL2 = new URL("http://" + getServerHost() + ":8080/jaxws-as3581/SimpleService2?wsdl");
      final Service service2 = Service.create(wsdlURL2, serviceName2);
      final EndpointIface2 port2 = service2.getPort(EndpointIface2.class);
      final String oneWayLookupString = port2.getString();
      assertEquals("Ahoj", oneWayLookupString);
   }

}
