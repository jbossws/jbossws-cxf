/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws2701;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * [JBWS-2701] @XmlSeeAlso and generated wsdl
 *
 * @author alessio.soldano@jboss.com
 * @since 30-Sep-2009
 */
public class JBWS2701TestCase extends JBossWSTest
{
   private final String endpointAddress = "http://" + getServerHost() + ":8080/jaxws-jbws2701/EndpointService/EndpointImpl";

   public static Test suite()
   {
      return new JBossWSTestSetup(JBWS2701TestCase.class, "jaxws-jbws2701.jar");
   }

   public void testWSDL() throws Exception
   {
      URL url = new URL(endpointAddress + "?wsdl");
      BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
      String line = br.readLine();
      StringBuilder sb = new StringBuilder();
      while (line != null)
      {
         sb.append(line);
         line = br.readLine();
      }
      assertTrue(sb.toString().contains("classA"));
   }

   public void testEndpoint() throws Exception
   {
      URL url = new URL(endpointAddress + "?wsdl");
      QName serviceName = new QName("http://org.jboss/test/ws/jbws2701", "EndpointService");
      Service service = Service.create(url, serviceName);
      Endpoint port = service.getPort(Endpoint.class);
      String s = "Hi";
      assertEquals(s, port.echo(s));
   }
}
