/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.noIntegration;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;

/**
 * Verifies a plain Apache CXF ws endpoint war can be deployed on
 * AS similarly as on a Tomcat instance (the Apache CXF libs from
 * the AS modules are used instead of embedding them in the war).
 * This is is NOT the suggest approach as any Java EE support is
 * actually disabled / skipped (including any JBossWS-CXF
 * integration additions, JSR-109, etc.)
 * 
 * Testcase provided here for the sake of verifying usage of AS as
 * a plain servlet container only, which is sometimes an easy
 * migration path for Apache CXF WS endpoints previously deployed
 * on Tomcat. 
 * 
 * @author alessio.soldano@jboss.com
 * @since 15-Apr-2013
 */
public class DisabledWSSubsystemTestCase extends JBossWSTest
{
   public static Test suite()
   {
      return new JBossWSCXFTestSetup(DisabledWSSubsystemTestCase.class, "jaxws-cxf-disabledWSSubsystem.war");
   }
   
   public void testEndpointInvocation() throws Exception
   {
      URL wsdlURL = new URL("http://" + getServerHost() + ":8080/jaxws-cxf-disabledWSSubsystem/services/Echo1?wsdl");
      Service service = Service.create(wsdlURL, new QName("http://org.jboss.ws.jaxws.cxf/noIntegration", "EchoService"));
      Echo echo = service.getPort(new QName("http://org.jboss.ws.jaxws.cxf/noIntegration", "EchoEndpointPort"), Echo.class);
      assertEquals("Foo", echo.echo("Foo"));
   }
   
   public void testServicesPage() throws Exception
   {
      URL url = new URL("http://" + getServerHost() + ":8080/jaxws-cxf-disabledWSSubsystem/services");
      InputStream is = url.openStream();
      assertNotNull(is);
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      try {
         StringBuilder sb = new StringBuilder();
         String line;
         while ((line = reader.readLine()) != null) {
            sb.append(line);
         }
         assertTrue(sb.toString().contains("Available SOAP services:"));
      } finally {
         reader.close();
      }
   }
}
