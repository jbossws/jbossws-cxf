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
package org.jboss.test.ws.jaxws.jbws1872;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBWS-1872] EJB3 WebService implementation must have @Remote (instead of @Local) Business interface
 *
 * @author richard.opalka@jboss.com
 */
public class JBWS1872TestCase extends JBossWSTest
{
   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS1872TestCase.class, "jaxws-jbws1872.jar");
   }

   public void testEJB1() throws Exception
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws1872/Bean1?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/jbws1872", "EJB3Bean1Service");
      Service service = Service.create(wsdlURL, serviceName);
      Client1 port = service.getPort(Client1.class);
      String retStr = port.echo("hello");
      assertEquals("bean1-hello", retStr);
   }

   public void testEJB2() throws Exception
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws1872/Bean2?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/jbws1872", "EJB3Bean2Service");
      Service service = Service.create(wsdlURL, serviceName);
      Client2 port = service.getPort(Client2.class);
      String retStr = port.echo("hello");
      assertEquals("bean2-hello", retStr);
   }

   public void testEJB3() throws Exception
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-jbws1872/Bean3?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/jbws1872", "EJB3Bean3Service");
      Service service = Service.create(wsdlURL, serviceName);
      Client3 port = service.getPort(Client3.class);
      String retStr = port.echo("hello");
      assertEquals("bean3-hello", retStr);
   }

}
