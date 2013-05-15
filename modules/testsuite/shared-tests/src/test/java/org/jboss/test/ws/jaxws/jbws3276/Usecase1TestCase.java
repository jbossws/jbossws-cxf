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

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBWS-3276] Tests anonymous POJO in web archive that contains web.xml with other endpoint.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public class Usecase1TestCase extends JBossWSTest
{

   public static Test suite()
   {
      return new JBossWSTestSetup(Usecase1TestCase.class, "jaxws-jbws3276-usecase1.war");
   }

   public void testAnonymousEndpoint() throws Exception
   {
      final QName serviceName = new QName("org.jboss.test.ws.jaxws.jbws3276", "AnonymousPOJOService");
      final URL wsdlURL = new URL("http://" + getServerHost() +  ":8080/jaxws-jbws3276-usecase1/AnonymousPOJOService?wsdl");
      final Service service = Service.create(wsdlURL, serviceName);
      final POJOIface port = service.getPort(POJOIface.class);
      final String result = port.echo("hello");
      assertEquals("hello from anonymous POJO", result);
   }

   public void testDeclaredEndpoint() throws Exception
   {
      final QName serviceName = new QName("org.jboss.test.ws.jaxws.jbws3276", "POJOImplService");
      final URL wsdlURL = new URL("http://" + getServerHost() +  ":8080/jaxws-jbws3276-usecase1/POJOService?wsdl");
      final Service service = Service.create(wsdlURL, serviceName);
      final POJOIface port = service.getPort(POJOIface.class);
      final String result = port.echo("hello");
      assertEquals("hello from POJO", result);
   }

}
